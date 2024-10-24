package com.example.velopath.destinations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

@Serializable
object Info

@Preview
@Composable
fun PrintInfo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        PrintHeader()
    }
}

@Composable
fun PrintHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            modifier = Modifier
                .size(48.dp)
        )
        Text(
            text = "About",
            textAlign = TextAlign.Start,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}
