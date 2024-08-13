package com.example.floatingbutton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MyFloatingComposable(
    hideOverlay: () -> Unit,
) {
    Button(
        onClick = { hideOverlay() },
        modifier = Modifier
            .padding(0.dp)

    ) {
        Text(
            text = "Close Overlay",
            modifier = Modifier.padding(0.dp)
        )
    }
}