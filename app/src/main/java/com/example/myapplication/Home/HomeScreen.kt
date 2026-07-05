package com.example.myapplication.Home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.runtime.Composable
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
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasLocationPermission = rememberLocationPermission()

    Scaffold(
        topBar = { HomeHeader(onOpenGenres = onOpenGenres) },
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
        HomeMap(
            hasLocationPermission = hasLocationPermission,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun HomeHeader(onOpenGenres: () -> Unit = {}) {
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
                text = "WilliWillsWissen",
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

        // Der Pfeil führt zur Genre-Auswahl.
        Text(
            text = "->",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.clickable(onClick = onOpenGenres)
        )
    }
}

@Composable
fun HomeMap(
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val startPosition = LatLng(50.9375, 6.9603)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPosition, 13f)
    }

    GoogleMap(
        modifier = modifier,
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
        musicSpots.forEach { spot ->
            MusicSpotMarker(spot = spot)
        }
    }
}

@Composable
fun MusicSpotMarker(spot: MusicSpot) {
    val markerIcon = remember(spot.title) {
        createMusicSpotIcon(spot)
    }

    Marker(
        state = MarkerState(position = spot.position),
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
            .padding(horizontal = 18.dp, vertical = 10.dp),
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
