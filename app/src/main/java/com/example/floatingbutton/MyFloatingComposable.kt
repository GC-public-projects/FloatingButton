package com.example.floatingbutton

import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun MyFloatingComposable(
    hideOverlay: () -> Unit,
    params: WindowManager.LayoutParams,
    windowManager: WindowManager,
    overlayView: View?
) {
    Button(
        onClick = { hideOverlay() },
        modifier = Modifier
            .padding(0.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    params.x += dragAmount.x.toInt()
                    params.y += dragAmount.y.toInt()
                    windowManager.updateViewLayout(overlayView, params)
                }
            }
    ) {
        Text(
            text = "Close Overlay",
            modifier = Modifier.padding(0.dp)
        )
    }
}