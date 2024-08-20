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

#### Overrided methods from Service mother class():
##### override fun onCreate()
Method that handle the initial setup when the service is created. windowManeger is affected the WINDOW_SERVICE in order to use it later to display the windows/overlays. \_savedStateRegistryController is attached to the service and restored in order to retrieve the former states of the ComposeView in case the service was killed and restarted.

###### override fun onBind(intent: Intent?)
Method not used in this case as the service must run regardless any activity

###### override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int
Method called each time startService() is called (from an activity or another component). In function of the extras of the intent used to start the service, the method "showOverlay()" or hideOverlay() will be called. This method return an integer code that will indicate the behavior of the service when it is killed. In this case, START_NOT_STICKY indicates that the service should not be restarted automatically.

###### override fun onDestroy()
Method called when the service is stopped by the app or by the system. To avoid Memory leaks, the overlay view is removed from windowManager and affected to null.The lifecycle event is setup to ON_DESTROY in order to clean all the active components.

#### Other personal methods 
##### private fun showOverlay()
Method called each time the intent that contains the extra : "INTENT_EXTRA_COMMAND_SHOW_OVERLAY" is launched to start the service (handled in "onStartCommand"). The lifecycle events ON_START and ON_RESUME are started in order to prepare the view to be created. 
- val params : constant that is affected the parameters required to add the view to windowManager. the fun getLayoutParams() will be explained later
- overlayView : it is affected a new ComposeView with our custom flaoting composable as content. setViewTreeLifecycleOwner(this@ComposeOverlayService) & setViewTreeSavedStateRegistryOwner(this@ComposeOverlayService) are applied to the ComposeView and are mandatory to implement the lifecycle events and the state management on it.
- windowManager : is added the view and the params just created before in the function. Once the view is added, it is displayed on the screen.

##### private fun hideOverlay() 
Method that remove the ComposeView with the Overlay composable from the screen. The lifecycle events ON_PAUSE and ON_STOP are started to replicate the behavior of an activity when it is paused or stopped.


It is called at 3 different places :
- called each time the intent that contains the extra : "INTENT_EXTRA_COMMAND_HIDE_OVERLAY" is launched to start the service (handled in "onStartCommand").
- also called from the button "Hide overlay" in the floating composable.
- called when onDestroy() is called.

##### private fun getLayoutParams(): WindowManager.LayoutParams
Method that returns a "WindowManager.LayoutParams" object affected to params. It sets the properties that control the appearance, positioning, and behavior of the overlay. The params of WindowManager.LayoutParams(...) between the braces cannot be named as it is a Java and non kotlin nested class. The class "LayoutParams" has an overloaded constructor, so many versions of it exist. We will use the version with these params here :

###### width = WindowManager.LayoutParams.WRAP_CONTENT
These constant specify that the width of the overlay should be just large enough to fit the content inside it. The overlay will not occupy more space than necessary.

Other possible ways to setup the width :
- raw value in pixels :  400 * resources.displayMetrics.density.toInt() ("dp" value transformed to pixel here)
- MATCH_PARENT : match the size of the parent element

###### 2. height
Works the same way the width

###### 3. window type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
TYPE_APPLICATION_OVERLAY is used for overlays that should appear on top of all other apps, but it requires the SYSTEM_ALERT_WINDOW permission on Android 8.0 (API level 26) and above. It is the only type that works with services and fit to work with ViewTreeLifecycleOwner

Other possible "non deprecated" ways to setup the type :
- TYPE_APPLICATION : the default window type that belongs the app
- TYPE_APPLICATION_PANEL : Used to show dialogs/popups that appears above the main app.
- TYPE_APPLICATION_ATTACHED_DIALOG : same than "APPLICATION_PANEL but belongs the state of a specific activity's main window"

- 4. window flag = 
- 5. format = 










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






