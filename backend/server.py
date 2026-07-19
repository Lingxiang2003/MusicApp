#!/usr/bin/env python3
import json
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse

HOST = "0.0.0.0"
PORT = 8080
DATA_FILE = Path(__file__).with_name("recommendations.json")
WAYPOINTS_FILE = Path(__file__).with_name("waypoints.json")
LOCK = threading.Lock()
NEW_MESSAGE = threading.Condition(LOCK)


def load_messages():
    if not DATA_FILE.exists():
        return []
    return json.loads(DATA_FILE.read_text(encoding="utf-8"))


def save_messages(messages):
    DATA_FILE.write_text(json.dumps(messages, ensure_ascii=False, indent=2), encoding="utf-8")


def load_waypoints():
    if not WAYPOINTS_FILE.exists():
        return []
    return json.loads(WAYPOINTS_FILE.read_text(encoding="utf-8"))


def save_waypoints(waypoints):
    WAYPOINTS_FILE.write_text(json.dumps(waypoints, ensure_ascii=False, indent=2), encoding="utf-8")


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path == "/waypoints":
            with LOCK:
                return self.respond(200, load_waypoints())
        if parsed.path not in ("/recommendations", "/recommendations/wait"):
            return self.respond(404, {"error": "not found"})
        query = parse_qs(parsed.query)
        recipient = query.get("recipient", [""])[0].casefold()
        after = int(query.get("after", ["0"])[0])

        def matching_messages():
            return [
                item for item in load_messages()
                if item["recipient"].casefold() == recipient and item["id"] > after
            ]

        with NEW_MESSAGE:
            messages = matching_messages()
            if parsed.path == "/recommendations/wait" and not messages:
                NEW_MESSAGE.wait(timeout=25)
                messages = matching_messages()
        self.respond(200, messages)

    def do_POST(self):
        parsed = urlparse(self.path)
        length = int(self.headers.get("Content-Length", "0"))
        if parsed.path == "/waypoints":
            try:
                payload = json.loads(self.rfile.read(length).decode("utf-8"))
                required = (
                    "owner", "name", "songTitle", "songArtist",
                    "latitude", "longitude"
                )
                if any(str(payload.get(key, "")).strip() == "" for key in required):
                    return self.respond(400, {"error": "missing field"})
                latitude = float(payload["latitude"])
                longitude = float(payload["longitude"])
                if not -90 <= latitude <= 90 or not -180 <= longitude <= 180:
                    return self.respond(400, {"error": "invalid coordinates"})
                with LOCK:
                    waypoints = load_waypoints()
                    item = {
                        "id": max((point["id"] for point in waypoints), default=0) + 1,
                        "owner": str(payload["owner"]).strip(),
                        "name": str(payload["name"]).strip(),
                        "description": str(payload.get("description", "")).strip(),
                        "songTitle": str(payload["songTitle"]).strip(),
                        "songArtist": str(payload["songArtist"]).strip(),
                        "latitude": latitude,
                        "longitude": longitude,
                    }
                    waypoints.append(item)
                    save_waypoints(waypoints)
                return self.respond(201, item)
            except (ValueError, json.JSONDecodeError):
                return self.respond(400, {"error": "invalid json"})

        if parsed.path != "/recommendations":
            return self.respond(404, {"error": "not found"})
        try:
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
            required = ("sender", "recipient", "title", "artist")
            if any(not str(payload.get(key, "")).strip() for key in required):
                return self.respond(400, {"error": "missing field"})
            with NEW_MESSAGE:
                messages = load_messages()
                payload = {key: str(payload[key]).strip() for key in required}
                payload["id"] = max((item["id"] for item in messages), default=0) + 1
                messages.append(payload)
                save_messages(messages)
                NEW_MESSAGE.notify_all()
            self.respond(201, payload)
        except (ValueError, json.JSONDecodeError):
            self.respond(400, {"error": "invalid json"})

    def do_DELETE(self):
        parsed = urlparse(self.path)
        parts = parsed.path.strip("/").split("/")
        if len(parts) != 2 or parts[0] != "waypoints":
            return self.respond(404, {"error": "not found"})
        try:
            waypoint_id = int(parts[1])
        except ValueError:
            return self.respond(400, {"error": "invalid id"})
        owner = parse_qs(parsed.query).get("owner", [""])[0]
        with LOCK:
            waypoints = load_waypoints()
            waypoint = next((item for item in waypoints if item["id"] == waypoint_id), None)
            if waypoint is None:
                return self.respond(404, {"error": "not found"})
            if waypoint["owner"].casefold() != owner.casefold():
                return self.respond(403, {"error": "only owner can delete"})
            save_waypoints([item for item in waypoints if item["id"] != waypoint_id])
        self.respond(200, {"deleted": waypoint_id})

    def respond(self, status, payload):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        return


if __name__ == "__main__":
    print(f"MOCO backend running on http://localhost:{PORT}")
    ThreadingHTTPServer((HOST, PORT), Handler).serve_forever()
