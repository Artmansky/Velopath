package com.app.velopath.destinations.routes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.app.velopath.R
import com.app.velopath.database.RouteItem
import com.app.velopath.handlers.isNetworkAvailable
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline


@Composable
fun AnimatedExpandableList(
    isAuthor: Boolean,
    initialItems: List<RouteItem>,
    onProfileDelete: () -> Unit,
    onDeleteFunction: (String, () -> Unit, () -> Unit) -> Unit,
    onDeleteLikeFunction: (String, () -> Unit, () -> Unit) -> Unit,
    isDarkMode: Boolean,
    context: Context
) {
    val itemsDisplay = remember { mutableStateListOf(*initialItems.toTypedArray()) }

    if (itemsDisplay.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getString(context, R.string.no_results),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        val expandedItems = remember {
            mutableStateListOf(*BooleanArray(itemsDisplay.size) { false }.toTypedArray())
        }

        val listState = rememberLazyListState()

        val mapStyleOptions = remember {
            if (isDarkMode) {
                MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map)
            } else {
                null
            }
        }

        LazyColumn(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            state = listState
        ) {
            itemsIndexed(itemsDisplay, key = { index, _ -> index }) { index, item ->
                ExpandedItem(
                    context = context,
                    mapStyleOptions = mapStyleOptions,
                    item = item,
                    isExpanded = expandedItems[index],
                    isAuthor = isAuthor,
                    onDeleteFunction = onDeleteFunction,
                    onDeleteLikeFunction = onDeleteLikeFunction,
                    onDelete = {
                        itemsDisplay.remove(item)
                        onProfileDelete()
                    },
                    onExpandedChange = { expandedItems[index] = it }
                )
            }
        }
    }
}

@Composable
fun ExpandedItem(
    context: Context,
    mapStyleOptions: MapStyleOptions?,
    item: RouteItem,
    isExpanded: Boolean,
    isAuthor: Boolean,
    onDeleteFunction: (String, () -> Unit, () -> Unit) -> Unit,
    onDeleteLikeFunction: (String, () -> Unit, () -> Unit) -> Unit,
    onDelete: () -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "")
    val polylines = PolyUtil.decode(item.overviewPolyline)
    val markers = mutableListOf<LatLng>()
    markers.add(polylines.first())
    markers.add(polylines.last())

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onExpandedChange(!isExpanded) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (!isAuthor) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Like Status",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable {
                                if (isNetworkAvailable(context)) {
                                    onDeleteLikeFunction(item.id, {
                                        Toast
                                            .makeText(
                                                context,
                                                getString(context, R.string.unliked),
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }, {})
                                    onDelete()
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            getString(context, R.string.no_service),
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.graphicsLayer(rotationZ = rotationAngle)
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = getString(context, R.string.route_display),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            compassEnabled = false,
                            myLocationButtonEnabled = false,
                            scrollGesturesEnabled = false,
                            zoomGesturesEnabled = true,
                            tiltGesturesEnabled = false,
                            rotationGesturesEnabled = false
                        ),
                        properties = MapProperties(
                            mapStyleOptions = mapStyleOptions,
                            isMyLocationEnabled = false,
                            isTrafficEnabled = false,
                            isIndoorEnabled = false
                        ),
                        cameraPositionState = CameraPositionState(
                            CameraPosition(
                                polylines[0],
                                12.5f,
                                0f,
                                0f
                            )
                        )
                    ) {
                        markers.forEach { latLng ->
                            Marker(
                                state = MarkerState(position = latLng)
                            )
                        }

                        if (polylines.size > 1) {
                            for (i in 0 until polylines.size - 1) {
                                val point1 = polylines[i]
                                val point2 = polylines[i + 1]
                                Polyline(
                                    points = listOf(point1, point2),
                                    color = Color.Red,
                                    width = 10f
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(
                                getString(context, R.string.distance_duration),
                                item.distance,
                                item.duration
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                val mapIntent = Intent(
                                    Intent.ACTION_VIEW, Uri.parse(item.navigationLink)
                                )
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            }) {
                                Text(getString(context, R.string.ride))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isAuthor) {
                                Button(onClick = {
                                    if (isNetworkAvailable(context)) {
                                        onDeleteFunction(item.id, {}, {})
                                        onDelete()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            getString(context, R.string.no_service),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }) {
                                    Text(getString(context, R.string.delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}