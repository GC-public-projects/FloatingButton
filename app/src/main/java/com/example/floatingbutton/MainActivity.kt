package com.example.floatingbutton

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.floatingbutton.ui.theme.FloatingButtonTheme

class MainActivity : ComponentActivity() {
    private val context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloatingButtonTheme {
                var showPermissionDialog by remember { mutableStateOf(false) }
                val modifyShowPermissionDialog = { bool: Boolean -> showPermissionDialog = bool }

                LaunchedEffect(Unit) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6.0 (Marshmallow) and above
                        if (!Settings.canDrawOverlays(context)) {
                            modifyShowPermissionDialog(true)
                        }
                    }
                }
                if(showPermissionDialog) {
                    PermissionDialog(
                        message = "\"Display over other apps\" permission required !",
                        onDismiss = { modifyShowPermissionDialog(false) },
                        onConfirm = { openOverlaySettings(); modifyShowPermissionDialog(false) }
                    )
                } else { MainScreen(context = context, modifyShowPermissionDialog)  }
            }
        }
    }
    private fun openOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}





