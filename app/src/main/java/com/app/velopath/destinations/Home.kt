package com.app.velopath.destinations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.velopath.mapsHandling.MapsHandling
import kotlinx.serialization.Serializable

@Serializable
object Home

@Composable
fun PrintHome(
    modifier: Modifier = Modifier,
    mapsHandler: MapsHandling,
    darkMode: Boolean,
    onClick: () -> Unit
) {
    mapsHandler.PrintMap(modifier = modifier, darkMode = darkMode, onClick = onClick)
}