package com.example.myapplication.Home

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.communication.MusicRecommendation
import com.example.myapplication.Recommendations.RecommendationMessage
import com.example.myapplication.Waypoints.Waypoint
import com.example.myapplication.Waypoints.WaypointRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun HomeScreen(
    onOpenGenres: () -> Unit = {},
    onOpenRecommendations: () -> Unit = {},
    onOpenWaypoints: () -> Unit = {},
    username: String = "WilliWillsWissen",
    receivedRecommendation: MusicRecommendation? = null,
    onRecommendationConsumed: (MusicRecommendation) -> Unit = {},
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasLocationPermission = rememberLocationPermission()
    val context = LocalContext.current
    val waypointRepository = remember(context) { WaypointRepository(context) }
    val savedWaypoints by WaypointRepository.sharedWaypoints.collectAsState()

    LaunchedEffect(receivedRecommendation) {
        receivedRecommendation?.let {
            viewModel.receiveRecommendation(it.toSong())
            onRecommendationConsumed(it)
        }
    }

    LaunchedEffect(username) {
        viewModel.startRecommendationUpdates(username)
    }

    LaunchedEffect(Unit) {
        runCatching { waypointRepository.refreshShared() }
    }

    Scaffold(
        topBar = {
            HomeHeader(
                username = username,
                onOpenGenres = onOpenGenres,
                onOpenRecommendations = onOpenRecommendations,
                onOpenWaypoints = onOpenWaypoints
            )
        },
        bottomBar = {
            MusicPlayer(
                isPlaying = uiState.isPlaying,
                expanded = uiState.expanded,
                currentSong = uiState.currentSong,
                queue = uiState.queue,
                onPlayPause = viewModel::togglePlayback,
                onExpand = viewModel::toggleQueue
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeMap(
                hasLocationPermission = hasLocationPermission,
                waypoints = savedWaypoints,
                modifier = Modifier.fillMaxSize()
            )

            uiState.receivedSong?.let { song ->
                ReceivedRecommendationCard(
                    song = song,
                    onDismiss = viewModel::dismissReceivedRecommendation,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                )
            }
        }
    }

    uiState.incomingRecommendation?.let { message ->
        IncomingRecommendationDialog(
            message = message,
            onDismiss = viewModel::dismissIncomingRecommendation,
            onListen = viewModel::playIncomingRecommendation
        )
    }
}

@Composable
fun IncomingRecommendationDialog(
    message: RecommendationMessage,
    onDismiss: () -> Unit,
    onListen: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Neuer Musik-Tipp",
                color = Color(0xFFC93434),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("${message.sender} empfiehlt dir:", color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message.title,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(message.artist, fontSize = 17.sp, color = Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Später") }
        },
        confirmButton = {
            TextButton(onClick = onListen) {
                Text("Anhören", color = Color(0xFFC93434), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ReceivedRecommendationCard(
    song: Song,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xEE111111), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Musik-Tipp erhalten",
                color = Color(0xFFC93434),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${song.title} - ${song.artist}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Zur Warteschlange hinzugefügt",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
        Text(
            text = "OK",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(10.dp)
        )
    }
}

@Composable
fun HomeHeader(
    username: String = "WilliWillsWissen",
    onOpenGenres: () -> Unit = {},
    onOpenRecommendations: () -> Unit = {},
    onOpenWaypoints: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Platzhalter fuer das Profilbild.
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = username,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Metal-Lover",
                fontSize = 12.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Tipps",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC93434),
            modifier = Modifier
                .clickable(onClick = onOpenRecommendations)
                .padding(8.dp)
        )

        Text(
            text = "Waypoint",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .clickable(onClick = onOpenWaypoints)
                .padding(horizontal = 6.dp, vertical = 8.dp)
        )

        Text(
            text = "→",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .clickable(onClick = onOpenGenres)
                .padding(start = 4.dp)
        )
    }
}

@Composable
fun HomeMap(
    hasLocationPermission: Boolean,
    waypoints: List<Waypoint> = emptyList(),
    modifier: Modifier = Modifier
) {
    val startPosition = LatLng(51.1657, 10.4515)
    val deviceLocation = rememberDeviceLocation(hasLocationPermission)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPosition, 5f)
    }

    // Wenn der GPS-Sensor eine Position liefert, bewegt sich die Karte dorthin.
    LaunchedEffect(deviceLocation) {
        deviceLocation?.let { position ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(position, 15f),
                durationMs = 800
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = MapStyleOptions(LIGHT_MAP_STYLE)
            ),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = hasLocationPermission,
                zoomControlsEnabled = true
            )
        ) {
            deviceLocation?.let { location ->
                CurrentLocationMarker(position = location)
            }

            musicSpots.forEach { spot ->
                MusicSpotMarker(spot = spot)
            }

            waypoints.forEach { waypoint ->
                WaypointMarker(waypoint)
            }
        }

        LocationStatus(deviceLocation = deviceLocation)
    }
}

@Composable
fun WaypointMarker(waypoint: Waypoint) {
    Marker(
        state = MarkerState(LatLng(waypoint.latitude, waypoint.longitude)),
        title = waypoint.name,
        snippet = buildString {
            append("Von ${waypoint.owner}")
            append(" · ${waypoint.songTitle} - ${waypoint.songArtist}")
            if (waypoint.description.isNotBlank()) append(" · ${waypoint.description}")
        },
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
        zIndex = 15f
    )
}

@Composable
fun CurrentLocationMarker(position: LatLng) {
    val markerState = remember(position) {
        MarkerState(position = position)
    }

    Marker(
        state = markerState,
        title = "Du bist hier",
        snippet = "Aktuelle GPS-Position",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
        zIndex = 20f
    )
}

@Composable
fun LocationStatus(deviceLocation: LatLng?) {
    val statusText = if (deviceLocation == null) {
        "Warte auf GPS-Signal"
    } else {
        String.format(
            java.util.Locale.US,
            "GPS aktiv: %.5f, %.5f",
            deviceLocation.latitude,
            deviceLocation.longitude
        )
    }

    // Kleine Anzeige fuer die Vorfuehrung: man sieht, ob der Sensor Daten liefert.
    Text(
        text = statusText,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(12.dp)
            .background(Color(0xCC111111), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@SuppressLint("MissingPermission")
@Composable
fun rememberDeviceLocation(hasLocationPermission: Boolean): LatLng? {
    val context = LocalContext.current
    var deviceLocation by remember { mutableStateOf<LatLng?>(null) }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            return@DisposableEffect onDispose {}
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                deviceLocation = LatLng(location.latitude, location.longitude)
            }
        }

        val bestLocationProvider = locationManager.findBestLocationProvider()

        // Wie in der Vorlesung: LocationManager + LocationListener + requestLocationUpdates.
        val minTime = 1000L
        val minDistance = 0f

        if (bestLocationProvider != null) {
            locationManager.requestLocationUpdates(
                bestLocationProvider,
                minTime,
                minDistance,
                listener,
                Looper.getMainLooper()
            )
        }

        onDispose {
            locationManager.removeUpdates(listener)
        }
    }

    return deviceLocation
}

fun LocationManager.findBestLocationProvider(): String? {
    return listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER
    ).firstOrNull { provider ->
        isProviderEnabled(provider)
    }
}

@Composable
fun MusicSpotMarker(spot: MusicSpot) {
    val markerIcon = remember(spot.title) {
        createMusicSpotIcon(spot)
    }
    val markerState = remember(spot.position) {
        MarkerState(position = spot.position)
    }

    Marker(
        state = markerState,
        title = spot.title,
        snippet = spot.genres,
        icon = markerIcon,
        anchor = Offset(0.16f, 0.5f),
        zIndex = 10f
    )
}

@Composable
fun MusicPlayer(
    isPlaying: Boolean,
    expanded: Boolean,
    currentSong: Song,
    queue: List<Song>,
    onPlayPause: () -> Unit,
    onExpand: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .height(if (expanded) 430.dp else 190.dp)
            .background(
                color = Color(0xFF111111),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        if (expanded) {
            QueueList(queue = queue, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AlbumCover()

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Jetzt laeuft",
                    color = Color(0xFFC93434),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentSong.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentSong.artist,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        PlayerProgress()
        Spacer(modifier = Modifier.height(14.dp))

        PlayerButtons(
            isPlaying = isPlaying,
            expanded = expanded,
            onPlayPause = onPlayPause,
            onExpand = onExpand
        )
    }
}

@Composable
fun AlbumCover() {
    Box(
        modifier = Modifier
            .size(58.dp)
            .background(
                color = Color(0xFFC93434),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "M",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PlayerProgress() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "1:24", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(Color.DarkGray, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(4.dp)
                    .background(Color(0xFFC93434), RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "3:18", color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun QueueList(
    queue: List<Song>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Naechste Songs",
            color = Color(0xFFC93434),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn {
            items(queue) { song ->
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerButtons(
    isPlaying: Boolean,
    expanded: Boolean,
    onPlayPause: () -> Unit,
    onExpand: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerButton(text = "<<", onClick = {})
        PlayerButton(
            text = if (isPlaying) "Pause" else "Play",
            onClick = onPlayPause,
            highlight = true
        )
        PlayerButton(text = ">>", onClick = {})
        PlayerButton(
            text = if (expanded) "Zu" else "Liste",
            onClick = onExpand
        )
    }
}

@Composable
fun PlayerButton(
    text: String,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    Box(
        modifier = Modifier
            .background(
                color = if (highlight) Color(0xFFC93434) else Color(0xFF2B2B2B),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun rememberLocationPermission(): Boolean {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.any { it }
    }

    // Beim Start fragt die App nach GPS-Zugriff, falls er noch fehlt.
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    return hasPermission
}

fun Context.hasLocationPermission(): Boolean {
    val hasFineLocation = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return hasFineLocation || hasCoarseLocation
}

fun createMusicSpotIcon(spot: MusicSpot): BitmapDescriptor {
    val bitmap = Bitmap.createBitmap(380, 108, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Der Marker sieht wie ein kleines Album mit kurzer Beschriftung aus.
    paint.color = AndroidColor.argb(245, 255, 255, 255)
    canvas.drawRoundRect(82f, 22f, 370f, 86f, 18f, 18f, paint)

    paint.color = AndroidColor.argb(70, 0, 0, 0)
    canvas.drawCircle(52f, 56f, 46f, paint)

    paint.color = spot.markerColor
    canvas.drawCircle(52f, 52f, 40f, paint)

    paint.color = AndroidColor.WHITE
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 25f
    canvas.drawText(spot.initials, 52f, 61f, paint)

    paint.textAlign = Paint.Align.LEFT
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textSize = 22f
    paint.color = AndroidColor.BLACK
    canvas.drawText(spot.title, 102f, 50f, paint)

    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textSize = 17f
    canvas.drawText(spot.genres, 102f, 72f, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

data class MusicSpot(
    val title: String,
    val genres: String,
    val position: LatLng,
    val initials: String,
    val markerColor: Int
)

// Musiker und Bands aus Köln, verteilt auf bekannte Orte in der Stadt.
val musicSpots = listOf(
    MusicSpot("Brings", "Kölsch-Rock", LatLng(50.9498, 6.9603), "BR", AndroidColor.rgb(201, 52, 52)),
    MusicSpot("Höhner", "Kölsch, Pop", LatLng(50.9369, 6.9583), "HO", AndroidColor.rgb(32, 32, 32)),
    MusicSpot("Klee", "Pop, Elektro", LatLng(50.9322, 6.9404), "KL", AndroidColor.rgb(96, 96, 96)),
    MusicSpot("Can", "Krautrock", LatLng(50.9444, 6.9369), "CA", AndroidColor.rgb(126, 42, 42)),
    MusicSpot("Floh de Cologne", "Polit-Rock", LatLng(50.9256, 6.9561), "FC", AndroidColor.rgb(45, 45, 45)),
    MusicSpot("WDR Big Band", "Jazz", LatLng(50.9413, 6.9578), "WB", AndroidColor.rgb(150, 56, 56))
)

private const val LIGHT_MAP_STYLE = """
[
  {"elementType":"geometry","stylers":[{"color":"#f4f4f4"}]},
  {"elementType":"labels.icon","stylers":[{"visibility":"off"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#9a9a9a"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#ffffff"}]},
  {"featureType":"administrative","elementType":"geometry","stylers":[{"visibility":"off"}]},
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#d6d6d6"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#ffffff"}]},
  {"featureType":"transit","stylers":[{"visibility":"off"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#eeeeee"}]}
]
"""

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
