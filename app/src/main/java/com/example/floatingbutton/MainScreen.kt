package com.example.floatingbutton

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(context: Context, modifyShowPermissionDialog: (Boolean) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.padding(100.dp))
        Button(onClick = {
            if (!Settings.canDrawOverlays(context)) {
                modifyShowPermissionDialog(true)
            } else {
                ComposeOverlayService.showOverlay(context)
            }
        }) {
            Text(text = "Show Overlay")
        }
        Button(onClick = {
            ComposeOverlayService.hideOverlay(context)
        }) {
            Text(text = "Hide Overlay")
        }
    }
}