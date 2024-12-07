package com.app.velopath.mapsHandling

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.velopath.R
import com.app.velopath.destinations.routes.routeItems
import com.app.velopath.handlers.ApiHandlers
import com.app.velopath.handlers.getCurrentLocation
import com.app.velopath.handlers.isLocationPermissionGranted
import com.app.velopath.handlers.isNetworkAvailable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline


class MapsHandling(private val context: Context) {
    private var defaultLocation: Pair<Double, Double> = Pair(-73.977001, 40.728847)
    private var apiHandler: ApiHandlers = ApiHandlers(context)

    @Composable
    fun PrintMap(modifier: Modifier, darkMode: Boolean, onClick: () -> Unit) {
        var hasPermission by remember { mutableStateOf(isLocationPermissionGranted(context)) }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted -> hasPermission = isGranted }
        )

        LaunchedEffect(hasPermission) {
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        ShowMap(modifier, darkMode, hasPermission, onClick)
    }

    @Composable
    private fun ShowMap(
        modifier: Modifier,
        darkMode: Boolean,
        hasPermission: Boolean,
        onClick: () -> Unit
    ) {
        val userLocation = remember {
            if (hasPermission) getCurrentLocation(context) else null
        } ?: defaultLocation

        val cameraPositionState = remember {
            CameraPositionState(
                CameraPosition(
                    LatLng(userLocation.first, userLocation.second),
                    15f,
                    0f,
                    0f
                )
            )
        }

        val mapStyleOptions = remember(darkMode) {
            if (darkMode) {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map)
            } else {
                null
            }
        }

        val mapProperties =
            remember {
                mutableStateOf(
                    MapProperties(
                        isMyLocationEnabled = hasPermission,
                        mapStyleOptions = mapStyleOptions
                    )
                )
            }

        val uiSettings =
            remember {
                MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    rotationGesturesEnabled = false
                )
            }

        val isDiscoverVisible = remember { mutableStateOf(false) }
        val isAddDialogVisible = remember { mutableStateOf(false) }
        val isMarkingEnabled = remember { mutableStateOf(false) }
        val isExtraButtonsVisible = remember { mutableStateOf(false) }
        val isMapLoaded = remember { mutableStateOf(false) }

        val markers = remember { mutableStateListOf<LatLng>() }
        val showMarkers = remember { mutableStateListOf<LatLng>() }
        val polylines = remember { mutableStateOf<List<LatLng>?>(null) }
        val showPolylines = remember { mutableStateOf<List<LatLng>?>(null) }

        Column(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.80f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = mapProperties.value,
                    uiSettings = uiSettings,
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        placeMarker(latLng, isMarkingEnabled, markers, polylines)
                    },
                    onPOIClick = { latLng ->
                        placeMarker(latLng.latLng, isMarkingEnabled, markers, polylines)
                    },
                    onMyLocationClick = { latLng ->
                        placeMarker(
                            LatLng(latLng.latitude, latLng.longitude),
                            isMarkingEnabled,
                            markers,
                            polylines
                        )
                    },
                    onMapLoaded = {
                        if (!isNetworkAvailable(context)) {
                            Toast.makeText(
                                context,
                                "Map may not load. Check Internet",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        isMapLoaded.value = true
                    }
                ) {
                    markers.forEach { latLng ->
                        Marker(
                            state = MarkerState(position = latLng)
                        )
                    }

                    showMarkers.forEach { latLng ->
                        Marker(
                            state = MarkerState(position = latLng)
                        )
                    }

                    polylines.value?.let { polylineList ->
                        if (polylineList.size > 1) {
                            for (i in 0 until polylineList.size - 1) {
                                val point1 = polylineList[i]
                                val point2 = polylineList[i + 1]
                                Polyline(
                                    points = listOf(point1, point2),
                                    color = MaterialTheme.colorScheme.primary,
                                    width = 10f
                                )
                            }
                        }
                    }

                    showPolylines.value?.let { polylineList ->
                        if (polylineList.size > 1) {
                            for (i in 0 until polylineList.size - 1) {
                                val point1 = polylineList[i]
                                val point2 = polylineList[i + 1]
                                Polyline(
                                    points = listOf(point1, point2),
                                    color = Color.Red,
                                    width = 10f
                                )
                            }
                        }
                    }

                    if (isAddDialogVisible.value) {
                        ShowNameDialog(isAddDialogVisible)
                    }

                    if (isDiscoverVisible.value) {
                        if (hasPermission) {
                            val pair = getCurrentLocation(context)
                            if (pair != null) {
                                ShowDiscoverDialog(
                                    isDiscoverVisible,
                                    isExtraButtonsVisible,
                                    LatLng(pair.first, pair.second)
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "User's location unknown",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "User's location unknown",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                if (!isMapLoaded.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp
                    )
                }
                Box(
                    modifier = modifier
                        .align(Alignment.TopStart)
                ) {
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        FloatingActionButton(
                            onClick = onClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                modifier = modifier.size(32.dp)
                            )
                        }
                    }
                }
                Box(
                    modifier = modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = 64.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            isMarkingEnabled.value = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = "Add Marker",
                            modifier = modifier.size(32.dp)
                        )
                    }
                }
                Box(
                    modifier = modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (hasPermission) {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newCameraPosition(
                                        CameraPosition(
                                            LatLng(
                                                userLocation.first,
                                                userLocation.second
                                            ), 15f, 0f, 0f
                                        )
                                    )
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "User's location unknown",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Follow Location",
                            modifier = modifier.size(32.dp)
                        )
                    }
                }

                if (isExtraButtonsVisible.value) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {
                                showMarkers.clear()
                                showPolylines.value = null
                                isExtraButtonsVisible.value = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .padding(start = 64.dp)
                    ) {
                        FloatingActionButton(
                            onClick = {


                            }
                        ) {
                            Text("Ride!")
                        }
                    }
                }
            }
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .weight(0.20f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            isDiscoverVisible.value = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(text = "Discover nearby routes")
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                isAddDialogVisible.value = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Text(text = "Add route")
                        }
                        Button(
                            onClick = {
                                if (markers.size != 0) {
                                    markers.removeLast()
                                    if (markers.size > 1) {
                                        apiHandler.getDirections(
                                            markers
                                        ) { newPolylines ->
                                            if (newPolylines.isNullOrEmpty()) {
                                                Toast.makeText(
                                                    context,
                                                    "Service not available.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            polylines.value = newPolylines
                                        }
                                    } else {
                                        polylines.value = null
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No markers are placed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                        ) {
                            Text(text = "Delete last")
                        }
                    }
                }
            }
        }
    }

    private fun placeMarker(
        latLang: LatLng,
        markingEnabled: MutableState<Boolean>,
        markers: MutableList<LatLng>,
        polylines: MutableState<List<LatLng>?>
    ) {
        if (markingEnabled.value) {
            markers.add(latLang)
            markingEnabled.value = false

            if (markers.size > 1) {
                apiHandler.getDirections(
                    markers
                ) { newPolylines ->
                    if (newPolylines.isNullOrEmpty()) {
                        Toast.makeText(
                            context,
                            "Service not available.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    polylines.value = newPolylines
                }
            } else {
                polylines.value = null
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowNameDialog(isVisible: MutableState<Boolean>) {
        var inputText by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { isVisible.value = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Name your new Route!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { isVisible.value = false }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        isVisible.value = false
                    }) {
                        Text("Add")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowDiscoverDialog(
        isVisible: MutableState<Boolean>,
        controlsVisible: MutableState<Boolean>,
        latLang: LatLng
    ) {
        ModalBottomSheet(
            onDismissRequest = { isVisible.value = false }
        ) {
            DirectionsList(routeItems, controlsVisible, context)
        }
    }
}