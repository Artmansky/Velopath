package com.app.velopath.destinations.routes

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.velopath.R
import com.app.velopath.database.RouteItem
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings


val routeItems = listOf(
    RouteItem(
        title = "Road near the lake",
        author = "John Doe",
        id = "1",
        navigationLink = "https://www.google.com/maps/dir/?api=1&origin=Toronto&destination=Montreal",
        startLang = 43.65107,
        startLong = -79.347015,
        endLang = 45.50169,
        endLong = -73.567253,
        overviewPolyline = "abcd1234xyz",
    ),
    RouteItem(
        title = "Take me here when you can",
        author = "Jane Smith",
        id = "2",
        navigationLink = "https://www.google.com/maps/dir/?api=1&origin=Vancouver&destination=Seattle",
        startLang = 49.28273,
        startLong = -123.120735,
        endLang = 47.606209,
        endLong = -122.332069,
        overviewPolyline = "efgh5678uvw",
    ),
    RouteItem(
        title = "My new route",
        author = "Alice Johnson",
        id = "3",
        navigationLink = "https://www.google.com/maps/dir/?api=1&origin=San+Francisco&destination=Los+Angeles",
        startLang = 37.774929,
        startLong = -122.419418,
        endLang = 34.052235,
        endLong = -118.243683,
        overviewPolyline = "ijkl91011rst",
    ),
    RouteItem(
        title = "Road near the park",
        author = "Robert Brown",
        id = "4",
        navigationLink = "https://www.google.com/maps/dir/?api=1&origin=Berlin&destination=Munich",
        startLang = 52.520008,
        startLong = 13.404954,
        endLang = 48.135124,
        endLong = 11.581981,
        overviewPolyline = "mnop121314abc",
    ),
    RouteItem(
        title = "100km's in one drive",
        author = "Emily Davis",
        id = "5",
        navigationLink = "https://www.google.com/maps/dir/?api=1&origin=London&destination=Brighton",
        startLang = 51.507351,
        startLong = -0.127758,
        endLang = 50.82253,
        endLong = -0.137163,
        overviewPolyline = "qrst151617xyz",
    )
)

@Composable
fun AnimatedExpandableList(itemsDisplay: List<RouteItem>, isDarkMode: Boolean, context: Context) {
    val isAuthor = false
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
                onExpandedChange = { expandedItems[index] = it }
            )
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
    onExpandedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "")

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
                                //Zaimplementowac Clicka do bazy
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
                        text = "Route display:",
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
                            zoomGesturesEnabled = false,
                            tiltGesturesEnabled = false,
                            rotationGesturesEnabled = false
                        ),
                        properties = MapProperties(
                            mapStyleOptions = mapStyleOptions,
                            isMyLocationEnabled = false,
                            isTrafficEnabled = false,
                            isIndoorEnabled = false
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = {
                            val mapIntent = Intent(
                                Intent.ACTION_VIEW, Uri.parse(item.navigationLink)
                            )
                            mapIntent.setPackage("com.google.android.apps.maps")

                            context.startActivity(mapIntent)
                        }) {
                            Text("Ride")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isAuthor) {
                            Button(
                                onClick = {},
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}