# MILESTONE 5 - Nutzbares MVP und Datenaustausch

## MVP

Die App bietet Anmeldung, Kartenansicht, Standort, Musik-Spots, Musikplayer,
Genre-Auswahl, einen Bereich zum Austausch von Musik-Tipps und öffentliche
Waypoints.

## Gemeinsame Waypoints

Über **Waypoint** öffnet sich eine eigene Waypoint-Seite. Nutzer*innen oder Bands
können dort einen Ort mit Name, optionaler Beschreibung sowie Breiten- und
Längengrad erstellen. Die Koordinaten können manuell eingegeben oder von der
aktuellen GPS-Position übernommen werden.

Zusätzlich wird beim Erstellen ein Song mit Titel und Interpret*in ausgewählt.
Die Musik ist Bestandteil des Waypoints und wird am Marker sowie in der
öffentlichen Waypoint-Liste angezeigt.

Ein neuer Waypoint wird per `POST /waypoints` an das gemeinsame Backend
übertragen. Andere Nutzer*innen laden dieselben Punkte per `GET /waypoints`.
Öffentliche Waypoints erscheinen als grüne Marker auf der Hauptkarte; Titel,
Beschreibung und Ersteller sind am Marker sichtbar.

Der Ersteller sieht am eigenen Waypoint die Schaltfläche **Löschen**. Das
Backend prüft den Benutzernamen und verhindert, dass andere Nutzer*innen einen
fremden Waypoint löschen.

## Datenaustausch zwischen Nutzer*innen

Der Hauptbildschirm enthält die separate Schaltfläche **Tipps**. Sie öffnet den
Bereich **Musik-Tipps**. Dort kann ein Song aus mehreren Titeln und
Interpret*innen ausgewählt und an einen anderen Benutzernamen gesendet werden.

Neue Empfehlungen erscheinen automatisch als Dialog in der Mitte des
Hauptbildschirms. Der Empfänger sieht Absender, Titel und Interpret*in und kann
**Später** oder **Anhören** auswählen. Mit **Anhören** wird die Empfehlung direkt
zum aktuell laufenden Song im Player.

Eine Empfehlung enthält:

- Absendername
- Empfängername
- Songtitel
- Interpret*in

Für das MVP dient der beim Login eingegebene Benutzername als Account-ID. Die
Namen werden ohne Beachtung der Groß- und Kleinschreibung verglichen.

## Kommunikationskonzept aus der Vorlesung

Die Umsetzung verwendet die in der Vorlesung "Adding Communication" gezeigte
Request-response-Architektur:

- Android-App als HTTP-Client
- `HttpURLConnection` für `GET` und `POST`
- lokaler HTTP-Server als gemeinsames Backend
- JSON als Datenformat

Die App sendet eine Empfehlung per `POST /recommendations`. Der Empfänger wartet
über `GET /recommendations/wait?recipient=...` auf neue Daten. Dieses Long
Polling liefert bei einer neuen Empfehlung sofort eine Antwort, ohne alle fünf
Sekunden neue Requests auszuführen. Der Bereich **Tipps** enthält zusätzlich
eine manuelle Übersicht über empfangene Empfehlungen.

## Backend starten

Vor der Demonstration im Projektordner ausführen:

```text
python3 backend/server.py
```

Der Server läuft auf Port `8080`. Android-Emulatoren erreichen den Host-Rechner
über `http://10.0.2.2:8080`. Die Daten werden lokal in
`backend/recommendations.json` und `backend/waypoints.json` gespeichert; diese
Dateien werden nicht committet.

## Demo mit zwei Emulatoren

1. Backend starten.
2. App auf beiden Emulatoren installieren und öffnen.
3. Auf Emulator A als `Alice`, auf Emulator B als `Bob` anmelden.
4. Auf Emulator A **Tipps** öffnen, `Bob` als Empfänger eingeben und
   **Musik-Tipp senden** auswählen.
5. Auf dem Hauptbildschirm von Emulator B erscheint die Empfehlung automatisch
   in einem Dialog.
6. **Anhören** auswählen. Der empfohlene Song erscheint im Musikplayer.

Waypoint-Demo:

1. Nutzer A öffnet **Waypoint**, gibt einen Namen ein, wählt Musik aus und
   übernimmt die aktuelle GPS-Position.
2. Nutzer A speichert den Waypoint.
3. Nutzer B öffnet **Waypoint** und wählt **Aktualisieren**.
4. Nach der Rückkehr zur Hauptkarte sieht Nutzer B den neuen grünen Marker.

## Technische Prüfung

```text
./gradlew testDebugUnitTest assembleDebug
```

Sowohl die Android-Build-Prüfung als auch ein realer POST-/GET-Test des Backends
wurden erfolgreich ausgeführt.
