package com.app.velopath.mapsHandling

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
import com.app.velopath.R
import com.app.velopath.database.RouteItem
import com.app.velopath.handlers.isNetworkAvailable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.CameraPositionState


@Composable
fun DirectionsList(
    itemsDisplay: List<RouteItem>,
    controlsVisible: MutableState<Boolean>,
    showMarkers: MutableList<LatLng>,
    showPolylines: MutableState<List<LatLng>?>,
    navigationLink: MutableState<String>,
    cameraPosition: CameraPositionState,
    onLikeClicked: (String, () -> Unit, () -> Unit) -> Unit,
    context: Context
) {
    if (itemsDisplay.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getString(context, R.string.no_results),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        val listState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            state = listState
        ) {
            itemsIndexed(itemsDisplay, key = { index, _ -> index }) { _, item ->
                DirectionItem(
                    context = context,
                    controlsVisible = controlsVisible,
                    item = item,
                    showMarkers = showMarkers,
                    showPolylines = showPolylines,
                    navigationLink = navigationLink,
                    cameraPosition = cameraPosition,
                    onLikeClicked = onLikeClicked
                )
            }
        }
    }
}


@Composable
fun DirectionItem(
    context: Context,
    controlsVisible: MutableState<Boolean>,
    showMarkers: MutableList<LatLng>,
    showPolylines: MutableState<List<LatLng>?>,
    navigationLink: MutableState<String>,
    cameraPosition: CameraPositionState,
    onLikeClicked: (String, () -> Unit, () -> Unit) -> Unit,
    item: RouteItem
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val isLiked = remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (!isLiked.value) Icons.Rounded.Add else Icons.Filled.Star,
                    contentDescription = "Like Status",
                    modifier = Modifier
                        .clickable {
                            if (isNetworkAvailable(context)) {
                                if (!isLiked.value) {
                                    isLiked.value = !isLiked.value
                                    onLikeClicked(item.id, {
                                        Toast.makeText(
                                            context,
                                            getString(context, R.string.liked),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }, {
                                        Toast.makeText(
                                            context,
                                            getString(context, R.string.already_liked),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                                } else {
                                    Toast.makeText(
                                        context,
                                        getString(context, R.string.already_liked),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    getString(context, R.string.no_service),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Button(onClick = {
                        showMarkers.clear()
                        showPolylines.value = PolyUtil.decode(item.overviewPolyline)
                        showMarkers.add(LatLng(item.startLang, item.startLong))
                        showMarkers.add(LatLng(item.endLang, item.endLong))
                        navigationLink.value = item.navigationLink
                        cameraPosition.move(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition(
                                    LatLng(
                                        item.startLang,
                                        item.startLong
                                    ), 12.5f, 0f, 0f
                                )
                            )
                        )
                        controlsVisible.value = true
                    }
                    ) {
                        Text(getString(context, R.string.show))
                    }
                }
            }
        }
    }
}