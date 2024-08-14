# FloatingButton
little JetPack compose app with the minimum features required to show and hide a button over the apps

### Project status : Workable, documentation in progress...

## target audience
This project is for Jetpack Compose initiated user

## Presentation
The goal of this demo is to explain the way to show and hide a composable (button) over all the apps. In order to keep the app the more simple possible, the button wont be draggable. Another app will be created later in order to show some advanced features like the way to drag the composView on the screen.

## Required
- 1 permission required : SYSTEM_ALERT_WINDOW (display over other apps)
- 1 service needs to be created : In order to be able to use the other apps when the button is workable
- 2 implementations required for the service ("LifecycleOwner" & "SavedStateRegistryOwner") : In order to use lifecycle-aware components (like Jetpack Compose, LiveData, etc.) in it, as by default services don't inherently have "lifecycle" & "state management" 
- 1 additional library needed "androidx.savedstate" : required to implement "SavedStateRegistryOwner" in the service
- No need to add the library "androidx.lifecycle" as by default in an empty jetpack compose project it is already implemented

## Warning


# Init

## Permissions
In AndroidManifest.xml
``` xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

## Services
In AndroidManifest.xml
``` xml
...
	</activity>
	<service android:name=".ComposeOverlayService"
            android:exported="false"
            android:permission="android.permission.SYSTEM_ALERT_WINDOW">
</service>
</application>

```
## Libraries
In build.gradle.kts
``` kotlin
dependencies {
	...
	implementation("androidx.savedstate:savedstate-ktx:1.2.1")
}
```

# Code

## ComposeOverlayService (class)

### Purpose
Service that will create the ComposeView as an overlay on the top of the screen.

### Components explanations

#### Inheritance and implementations
The class inherits of Service and implements 2 interfaces
- Service() : all services should inherit from Service class
- LifecycleOwner : interface that will help us to implement the necessary attributes (only one) in order to handle the lifecycle for the ComposeView in our service
- SavedStateRegistryOwner : interface that will help us to implement the necessary attributes (only one) in order to handle the state management for the ComposeView in our service

#### Attributes
 - private val \_lifecycleRegistry : private attribute used to initialise the public attribute 
"lifecycle" 
- lifecycle : attribute implemented by the interface "LifecycleOwner". We will setup the private attribute on each event of our service and before the creation of the ComposeView to ensure that UI components react appropriately to lifecycle events.
 - private val \_savedStateRegistryController : private attribute used to initialise the public attribute "savedStateRegistry" 
 - savedStateRegistry : attribute implemented by the interface "SavedStateRegistryOwner". We will setup setup the private attribute in the "onCreate" event to ensure that any stateful composable within MyFloatingComposable can save and restore its state.
 - private lateinit var windowManager : critical component that will allow us to add, update or remove views (UI elements) independent of any specific activity.
 - private var overlayView : View that will be affected the ComposeView created later in the service 

#### methods :
- override fun onCreate() : Method that handle the initial setup when the service is created. windowManeger is affected the WINDOW_SERVICE in order to use it later to display the windows/overlays. \_savedStateRegistryController is attached to the service and restored in order to retrieve the former states of the ComposeView in case the service was killed and restarted.

- override fun onBind(intent: Intent?) : Method not used in this case as the service must run regardless any activity

- override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int : Method called each time startService() is called (from an activity or another component). In function of the extras of the intent used to start the service, the method "showOverlay()" or hideOverlay() will be called. This method return an integer code that will indicate the behavior of the service when it is killed. in this case, START_NOT_STICKY indicates that the service should not be restarted automatically.









### Content
- create Kotlin class/file in Main package named "ComposeOverlayService"
``` kotlin
class ComposeOverlayService :
    Service(),
    LifecycleOwner,
    SavedStateRegistryOwner {
    private val _lifecycleRegistry = LifecycleRegistry(this)
    private val _savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry =
        _savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle = _lifecycleRegistry

    lateinit var windowManager: WindowManager
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
                MyFloatingComposable(::hideOverlay)
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
//            400 * resources.displayMetrics.density.toInt(),
//            400 * resources.displayMetrics.density.toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

    }

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

}

```




## MainActivity (class)

### Purpose
- call the alert dialog to 






