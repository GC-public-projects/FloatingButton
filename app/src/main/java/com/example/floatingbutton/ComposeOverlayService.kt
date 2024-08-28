package com.example.floatingbutton

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class ComposeOverlayService :
    Service(),
    LifecycleOwner,
    SavedStateRegistryOwner {
    companion object {
        private const val INTENT_EXTRA_COMMAND_SHOW_OVERLAY =
            "INTENT_EXTRA_COMMAND_SHOW_OVERLAY"
        private const val INTENT_EXTRA_COMMAND_HIDE_OVERLAY =
            "INTENT_EXTRA_COMMAND_HIDE_OVERLAY"

        private fun startService(context: Context, command: String) {
            val intent = Intent(context, ComposeOverlayService::class.java)
            intent.putExtra(command, true)
            context.startService(intent)
        }

        internal fun showOverlay(context: Context) {
            startService(context, INTENT_EXTRA_COMMAND_SHOW_OVERLAY)
        }

        internal fun hideOverlay(context: Context) {
            startService(context, INTENT_EXTRA_COMMAND_HIDE_OVERLAY)
        }
    }
    private val _lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle = _lifecycleRegistry
    private val _savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        _savedStateRegistryController.savedStateRegistry


    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        _savedStateRegistryController.performAttach()
        _savedStateRegistryController.performRestore(null)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw RuntimeException("bound mode not supported")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.hasExtra(INTENT_EXTRA_COMMAND_SHOW_OVERLAY)) {
            showOverlay()
        }
        if (intent.hasExtra(INTENT_EXTRA_COMMAND_HIDE_OVERLAY)) {
            hideOverlay()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private fun showOverlay() {
        if (overlayView != null) return

        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val params = getLayoutParams()

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ComposeOverlayService)
            setViewTreeSavedStateRegistryOwner(this@ComposeOverlayService)
            setContent {
                MyFloatingComposable(
                    ::hideOverlay,
                    params,
                    windowManager,
                    overlayView
                )
            }
        }

        windowManager.addView(overlayView, params)
    }

    private fun hideOverlay() {
        Log.i("MYLOG", "hideOverlay()")
        if (overlayView == null) {
            Log.i("MYLOG", "overlay not shown - aborting")
            return
        }
        windowManager.removeView(overlayView)
        overlayView = null

        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        _lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    private fun getLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

    }
}