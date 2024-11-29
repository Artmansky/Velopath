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
import androidx.compose.material.icons.outlined.Star
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings

data class RouteItem(
    val title: String,
    val author: String,
    val id: String,
    val navigationLink: String,
    val isExpanded: Boolean = false
)

val routeItems = listOf(
    RouteItem(
        title = "Introduction to Kotlin",
        author = "John Doe",
        id = "1",
        navigationLink = "https://www.google.com/maps/dir/?api=1&origin=Toronto&destination=Montreal"
    ),
    RouteItem(
        title = "Advanced Compose Techniques",
        author = "Jane Smith",
        id = "2",
        navigationLink = "/advanced-compose"
    ),
    RouteItem(
        title = "Understanding Coroutines",
        author = "Alice Johnson",
        id = "3",
        navigationLink = "/understanding-coroutines"
    ),
    RouteItem(
        title = "State Management in Jetpack Compose",
        author = "Robert Brown",
        id = "4",
        navigationLink = "/state-management"
    ),
    RouteItem(
        title = "Navigation in Compose",
        author = "Emily Davis",
        id = "5",
        navigationLink = "/navigation-in-compose"
    )
)

@Composable
fun AnimatedExpandableList(itemsDisplay: List<RouteItem>, isDarkMode: Boolean, context: Context) {
    val isAuthor = true
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
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Like",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable {
                                //Zaimplementowac onclicka
                            }
                    )
                }
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