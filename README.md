# FloatingButton
little JetPack compose app with the minimum features required to show and hide a draggable button over the apps

### Project status : Workable, documentation completed

## target audience
This project is for Jetpack Compose initiated user

## Presentation
The goal of this demo is to explain the way to show and hide a draggable composable (button) over all the apps. In order to gain in understanding, almost only mandatory components will be used.

## Overview
- 1 : Content of the main screen
- 2 : when app and ComposeView running
- 3 : when app is closed and ComposeView running
<img src="/app/screenshots/1.png" alt="Mainscreen" height="400">&emsp;
<img src="/app/screenshots/2.png" alt="Mainscreen & overlay button" height="400">&emsp;
<img src="/app/screenshots/3.png" alt="overlay button with app closed" height="400">

## Required
- 1 permission required : SYSTEM_ALERT_WINDOW (display over other apps)
- 1 service needs to be created : In order to be able to use the other apps when the button is workable
- 2 implementations required for the service ("LifecycleOwner" & "SavedStateRegistryOwner") : In order to use lifecycle-aware components (like Jetpack Compose, LiveData, etc.) in it, as by default services don't inherently have "lifecycle" & "state management" 
- No need to add the libraries "androidx.lifecycle" and "androidx.savedstate" (sub library of androidx.activity:activity-compose) as by default in an empty jetpack compose project they are already implemented.

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

### Content
- create Kotlin Class/File in Main package named "ComposeOverlayService"
``` kotlin
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
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

    }
}
```

### Components explanations

### Inheritance and implementations
The class inherits of Service and implements 2 interfaces
- `Service()` : all services should inherit from Service class
- `LifecycleOwner` : interface that will help us to implement the necessary attributes (only one) in order to handle the lifecycle for the ComposeView in our service
- `SavedStateRegistryOwner` : interface that will help us to implement the necessary attributes (only one) in order to handle the state management for the ComposeView in our service

### Attributes
 - `private val \_lifecycleRegistry` : private attribute used to initialise the public attribute 
"lifecycle" 
- `lifecycle` : attribute implemented by the interface "LifecycleOwner". We will setup the private attribute on each event of our service and before the creation of the ComposeView to ensure that UI components react appropriately to lifecycle events.
 - `private val \_savedStateRegistryController` : private attribute used to initialise the public attribute "savedStateRegistry" 
 - `savedStateRegistry` : attribute implemented by the interface "SavedStateRegistryOwner". We will setup setup the private attribute in the "onCreate" event to ensure that any stateful composable within MyFloatingComposable can save and restore its state.
 - `private lateinit var windowManager` : critical component that will allow us to add, update or remove views (UI elements) independent of any specific activity.
 - `private var overlayView` : View that will be affected the ComposeView created later in the service 


### Companion object
Functions and parameters in relation with the service that will be needed to start it and give it some instructions to do.

#### `private const val INTENT_EXTRA_COMMAND_SHOW_OVERLAY` & `private const val INTENT_EXTRA_COMMAND_HIDE_OVERLAY` :
Constants used as extra of the intents we will create in order to start the service.

#### `private fun startService(context: Context, command: String)` :
method used to start the service. the params are the context of the MainActivity and the extra (string) of the intent. 

- `val intent = Intent(context, ComposeOverlayService::class.java)` : The intent is setup with the context of the activity from it is called an the service. 

- `intent.putExtra(command, true)` : the extra is added in order to indicate to the service what it needs to do when the method "onStartCommand" is called. 

- `context.startService(intent)` : Once the intent is setup, the Service is started from the context of the activity it is called by using "context.startService(intent)" with the intent as param. 

In Android, by design, only one instance of a particular Service can exist at a time. So the 1st time the service is started it will be instanciated ,then "onCreate()" and "onStartCommand" will be called and once the service is running, when the service is started, only "onStartCommand" will be called in the existing instance.

#### `internal fun showOverlay(context: Context)` & `internal fun hideOverlay(context: Context)` :
methods called from the MainScreen that will call startService(context: Context, command: String) with the context of the activity from it is called and the extra dedicated to the method.




### Overrided methods from Service mother class():
#### `override fun onCreate()` :
Method that handle the initial setup when the service is created. windowManeger is affected the WINDOW_SERVICE in order to use it later to display the windows/overlays. \_savedStateRegistryController is attached to the service and restored in order to retrieve the former states of the ComposeView in case the service was killed and restarted.

#### `override fun onBind(intent: Intent?)` :
Method not used in this case as the service must run regardless any activity

#### `override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int` :
Method called each time startService() is called (from an activity or another component). In function of the extras of the intent used to start the service, the method "showOverlay()" or hideOverlay() will be called. This method return an integer code that will indicate the behavior of the service when it is killed. In this case, START_NOT_STICKY indicates that the service should not be restarted automatically.

#### `override fun onDestroy()` :
Method called when the service is stopped by the app or by the system. To avoid Memory leaks, the overlay view is removed from windowManager and affected to null.The lifecycle event is setup to ON_DESTROY in order to clean all the active components.

### Other personal methods 
#### `private fun showOverlay()` :
Method called each time the intent that contains the extra : "INTENT_EXTRA_COMMAND_SHOW_OVERLAY" is launched to start the service (handled in "onStartCommand"). The lifecycle events ON_START and ON_RESUME are started in order to prepare the view to be created. 
- `val params` : constant that is affected the parameters required to add the view to windowManager. the fun getLayoutParams() will be explained later
- `overlayView` : it is affected a new ComposeView with our custom flaoting composable as content. setViewTreeLifecycleOwner(this@ComposeOverlayService) & setViewTreeSavedStateRegistryOwner(this@ComposeOverlayService) are applied to the ComposeView and are mandatory to implement the lifecycle events and the state management on it.
- `windowManager` : is added the view and the params just created before in the function. Once the view is added, it is displayed on the screen.

#### `private fun hideOverlay()` :
Method that remove the ComposeView with the Overlay composable from the screen. The lifecycle events ON_PAUSE and ON_STOP are started to replicate the behavior of an activity when it is paused or stopped.


It is called at 3 different places :
- called each time the intent that contains the extra : "INTENT_EXTRA_COMMAND_HIDE_OVERLAY" is launched to start the service (handled in "onStartCommand").
- also called from the button "Hide overlay" in the floating composable.
- called when onDestroy() is called.

#### `private fun getLayoutParams(): WindowManager.LayoutParams` :
Method that returns a "WindowManager.LayoutParams" object affected to params. It sets the properties that control the appearance, positioning, and behavior of the overlay. The params of WindowManager.LayoutParams(...) between the braces cannot be named as it is a Java and non kotlin nested class. The class "LayoutParams" has many overloaded constructors. We will use the version with these params here :

##### 1. `width = WindowManager.LayoutParams.WRAP_CONTENT` :
These constant specify that the width of the overlay should be just large enough to fit the content inside it. The overlay will not occupy more space than necessary.

Other possible ways to setup the width :
| LAYOUTPARAMS | DESCRIPTION |
|-------------|--------------|
| 600 (exemple) |  raw value in pixels, 400 * resources.displayMetrics.density.toInt() (exemple 2 : "dp" value transformed to pixel here) |
| MATCH_PARENT | match the size of the parent element |

##### 2. `height = WindowManager.LayoutParams.WRAP_CONTENT` :
Works the same way than the width

##### 3. `window type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` :
`TYPE_APPLICATION_OVERLAY` is used for overlays that should appear on top of all other apps, but it requires the SYSTEM_ALERT_WINDOW permission on Android 8.0 (API level 26) and above. It is the only type that works with services and fit to work with ViewTreeLifecycleOwner

Other possible "non deprecated" ways to setup the type :
| TYPE | DESCRITPION |
|------|-------------|
|TYPE_APPLICATION | the default window type that belongs the app |
|TYPE_APPLICATION_PANEL | Used to show dialogs/popups that appears above the main app. |
|YPE_APPLICATION_ATTACHED_DIALOG | same than "APPLICATION_PANEL but belongs the state of a specific activity's main window |

##### 4. `window flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL` :
The flags control the behavior of the window, they can be combined by using the bitwise operator "or".
- `FLAG_NOT_TOUCH_MODAL` : This flag allows touches outside the overlay's bounds to pass through to underlying windows. Without this flag, the overlay might consume all touch events, even those outside its visible area.

Other possible flags :
| FLAG | DESCRIPTION |
|------|-------------|
| FLAG_NOT_FOCUSABLE | This flag indicates that the window cannot receive input focus, which means it cannot intercept key events or other input. The window will still receive touch events, but they will be passed through to the underlying window. |
| FLAG_NOT_TOUCHABLE | When this flag is set, the window will not receive any touch events. All touch events will pass through to windows below it. |
| FLAG_SHOW_WHEN_LOCKED | This flag allows the window to be shown even when the screen is locked. |
| FLAG_KEEP_SCREEN_ON | This flag prevents the screen from dimming or turning off while the window is visible. |
| FLAG_DIM_BEHIND | This flag dims everything behind the window, making the current window stand out more prominently. |
| FLAG_BLUR_BEHIND | This flag blurs everything behind the window. It is deprecated because blurring the background is now handled in different ways in modern Android versions. |
| FLAG_FULLSCREEN | This flag requests that the window is shown in full-screen mode, meaning it will hide the status bar and other system decorations. |
| FLAG_LAYOUT_IN_SCREEN | Allows the window to extend into the screen's non-decorated areas, like the status bar. |
| FLAG_LAYOUT_NO_LIMITS | This flag allows the overlay to extend beyond the screen edges. This can be useful if you want the overlay to partially or fully extend off the screen, which might be necessary for certain UI designs or animations. |
| FLAG_ALT_FOCUSABLE_IM | This flag alters how the window interacts with the soft keyboard. If the flag is set, the window won't automatically receive focus when the keyboard is shown. |
| FLAG_TRANSLUCENT_STATUS | This flag makes the status bar translucent, allowing the window to draw under it with a faded effect. |
| FLAG_TRANSLUCENT_NAVIGATION | Similar to FLAG_TRANSLUCENT_STATUS, but for the navigation bar. It makes the navigation bar translucent, allowing the window to draw under it. |
| FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS | This flag indicates that the window should be responsible for drawing the background for the system bars (status and navigation bars). |
| FLAG_SECURE | This flag prevents the window's content from being captured in screenshots or by screen recording. |
| FLAG_TOUCHABLE_WHEN_WAKING | Allows the window to receive touch events when the screen is waking up; |
| FLAG_LAYOUT_INSET_DECOR | Allows the window to be laid out within the decor view, which includes the status bar and other system decorations. |
| FLAG_WATCH_OUTSIDE_TOUCH | This flag allows the window to receive a callback when a touch event happens outside its bounds, but only if it doesn't consume the event itself. |
| FLAG_HARDWARE_ACCELERATED | Forces the window to be hardware-accelerated. |
| FLAG_IGNORE_CHEEK_PRESSES | This flag prevents the window from responding to touch events that are likely accidental, such as when the user inadvertently touches the screen with their cheek during a phone call. |


##### 5. `format = PixelFormat.TRANSLUCENT` :
`PixelFormat.TRANSLUCENT` : This pixel format means that the window can be partially transparent. The overlay will be drawn on top of other windows, but the background behind it can still be partially visible. This is useful if your overlay has transparent regions.

Other possible PixelFormats : 
| PIXEL FORMAT           | DESCRIPTION                              |
|------------------------|------------------------------------------|
| PixelFormat.OPAQUE    | No transparency, best for performance.  |
| PixelFormat.TRANSPARENT| Fully transparent windows.             |
| PixelFormat.RGBA_8888 | High-quality with full color and transparency support. |
| PixelFormat.RGB_888   | High-quality without transparency.      |
| PixelFormat.RGB_565   | Lower quality, better performance.      |
| PixelFormat.A_8       | Alpha channel only, useful for masks.   |
| PixelFormat.L_8       | Grayscale without transparency.         |
| PixelFormat.LA_88     | Grayscale with transparency.            |
| PixelFormat.RGBA_4444 | Deprecated, low color fidelity.         |

### .apply { gravity = Gravity.CENTER }
Ensure the ComposeView is displayed on the middle of the screen. The gravity attributes can be combined by using the bitwise operator "or" like the "flags"

Other Gravity attributes : 

| GRAVITY ATTRIBUTE              | DESCRIPTION |
|--------------------------------|-------------|
| Gravity.TOP                    | Aligns the window to the top of the screen or parent.                                               |
| Gravity.BOTTOM                 | Aligns the window to the bottom of the screen or parent.                                             |
| Gravity.LEFT                   | Aligns the window to the left side of the screen or parent.                                         |
| Gravity.RIGHT                  | Aligns the window to the right side of the screen or parent.                                        |
| Gravity.CENTER                 | Centers the window horizontally and vertically within the screen or parent.                         |
| Gravity.CENTER_HORIZONTAL      | Centers the window horizontally within the screen or parent.                                        |
| Gravity.CENTER_VERTICAL        | Centers the window vertically within the screen or parent.                                          |
| Gravity.FILL                   | Expands the window to fill the entire screen or parent.                                              |
| Gravity.FILL_HORIZONTAL        | Expands the window to fill the entire width of the screen or parent.                                |
| Gravity.FILL_VERTICAL          | Expands the window to fill the entire height of the screen or parent.                               |
| Gravity.START                  | Aligns the window to the start of the screen or parent, considering the layout direction (LTR or RTL). |
| Gravity.END                    | Aligns the window to the end of the screen or parent, considering the layout direction (LTR or RTL).   |
| Gravity.CLIP_VERTICAL          | Clips the vertical position to fit within the screen or parent.                                      |
| Gravity.CLIP_HORIZONTAL        | Clips the horizontal position to fit within the screen or parent.                                    |
| Gravity.HORIZONTAL_GRAVITY_MASK| Mask for horizontal gravity constants.                                                              |
| Gravity.VERTICAL_GRAVITY_MASK  | Mask for vertical gravity constants.                                                                |


Some other attributs of the "LayoutParams" nested class that are also not present in the constructor can be setup to : 

| LAYOUTPARAMS ATTRIBUTE   | DESCRIPTION |
|--------------------------|-------------|
| x                        | The x-coordinate of the window's position on the screen.                                       |
| y                        | The y-coordinate of the window's position on the screen.                                       |
| windowAnimations         | The animation style resource for the window.                                                   |
| dimAmount                | The amount of dimming behind the window when it is displayed.                                  |
| screenBrightness         | The desired brightness of the screen for the window (0.0 to 1.0).                              |
| alpha                    | The opacity level of the window (0.0 for fully transparent, 1.0 for fully opaque).             |
| softInputMode            | How the window interacts with the soft input area (keyboard).                                  |
| token                    | The token of another window that this window is attached to.                                   |
| horizontalMargin         | Horizontal margin for the window, relative to the screen size.                                 |
| verticalMargin           | Vertical margin for the window, relative to the screen size.                                   |
| title                    | The title of the window, used primarily for debugging purposes.                                |
| packageName              | The package name of the application that owns this window.                                     |
| screenOrientation        | The preferred screen orientation for the window.                                               |
| preferredDisplayModeId   | The ID of the preferred display mode for the window.                                           |
| buttonBrightness         | The brightness of the button backlights for the window (0.0 to 1.0).                           |
| rotationAnimation        | The rotation animation type used when rotating the device while this window is displayed.      |
| systemUiVisibility       | Flags for controlling the visibility of system UI components (status bar, navigation bar, etc.).|
| preferredRefreshRate     | The preferred refresh rate for the window's display.                                           |


## MyFloatingComposable (composable)
Button that will be added to the ComposeView of the service

### Purpose
Display a floating button over all the other apps. As the ComposeView was setup with the param type "TYPE_APPLICATION_OVERLAY", all its content is floating.

### Content
create in the main package a kotlin Class/File named : "MyFloatingComposable "

``` kotlin
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
```

### Components explanations

- The fun `hideOverlay()` is passed as param in order to use it when the button "Close Overlay" is used.


## MainActivity (class)

### Purpose
- Triggers an alert permission dialog in order to allow the app to be displayed over the other apps via the dedicated settings "Display over other apps"

### Content
Modify the "MainActivity" file like that

``` kotlin
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
```

### Components explanations

- `private val context` : the context of the MainActivity needs to be used in the intent to create the service.

- `var showPermissionDialog` & `val modifyShowPermissionDialog` flag with its setter to show or not a dialog box in order to go to the settings "Display over other apps"

- `LaunchedEffect(Unit)` : the content will test if the required permission is allowed by using "Settings.canDrawOverlays" and will set the flag to show the alert dialog to true.

As not any state is used as param we ensure when the MainActivityContent is recomposed, the content of the "LaunchedEffect" is not executed again. 
The test of the Android version is usefull only if the minSDK version is lesser than 6.


`PermissionDialog` : composable we will explain later in order to access the overlay settings of the app. It is displayed if "showPermissionDialog" = true

`MainScreen` : composable we will explain later. It is the only GUI of the app, It contents 2 buttons in order to display or hide our overlay button via the service.


`private fun openOverlaySettings()` : Method that handle the display of the settings "Display over other apps"


## PermissionDialog (composable)

### Purpose
Shows an alert dialog in order to go "Display over other apps" settings activate the permission.

### Content
Create a Kotlin file on the main package named : "PermissionDialog"

``` kotlin
@Composable
fun PermissionDialog(
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Permission Required") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = "Overlay Settings")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Deny")
            }
        }
    )
}
```

### Components explanations

- `onDismiss` : High order function used in the dismissButton. When it is called from the MainActivity, "{ modifyShowPermissionDialog(false) }" is assigned to it

- `onConfirm` : High order function used in the confirmButton When it is called from the MainActivity, "{ openOverlaySettings(); modifyShowPermissionDialog(false) }" is assigned to it 


## MainScreen (composable)

### Purpose 
Display 2 buttons in order to show and hide the overlay button via the service

### Content
In the main package create a kotlin file named "MainScreen "

``` kotlin
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
```

### Components explanations

The Composable takes 2 params : the `Context` of the main activity in order to start the service and the function `modifyShowPermissionDialog` in order to allow the app the display overlay in the settings in case the permission wasn't accepted before.

- `Button "Show Overlay"` : Shows the alert dialog if the overlay permission is not accepted or start the service if it is. the function in the service is stopped by using "return" in case the overlayView is already assigned"

- `Button "Hide Overlay"` : starts the service and asks it to hide the overlay. The function inside the service will remove the ComposeView only if it is not null.

### As of now the project works but the button is not draggable

# Making the button draggable
There a re 2 ways 2 make a composView draggable :
- `overlayView?.setOnTouchListener` : works only when the view is dragged from a non clickable component like a button
- `Modifier.pointerInput` : works fine with clickable components. all the gestures are captured and modify in real time the x and y params of the view. Whatever "Modifier.pointerInput" is setup on the button itself or anther parent composable, the view remains draggable by non or clickable components.


As whe have a button, the 2nd methos will be used.

## MyFloatingComposable (composable)

### Content
Modify "MyFloatingComposable" like that :

``` kotlin
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
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    Button(
        onClick = { hideOverlay() },
        modifier = Modifier
            .padding(0.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y

                    // Update the layout params of the overlayView
                    params.x = offsetX.toInt()
                    params.y = offsetY.toInt()
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
```

### Components explanations
#### New params 
- `params` : "WindowManager.LayoutParams" object setup with the view. We need the existing object in order to update the already existing x and y params following the drag gestures.
- `windowManager` : We need the existing object in order to apply the modified params to it.
- `overlayView` : windowManager needs also the overlayView object to update it.

#### Variables
- `var offsetX` & `var offsetY` : remember by how much the drag gesture was done.

#### Actions
- `Modifier..pointerInput(Unit)` : method of the modifier in which `detectDragGestures` will by used. The drag gesture is captured in our "offsetX" and "offsetY" variables then our "params" object is updated by adding to it the x and y values. Once done, the view position is updated by using `windowManager.updateViewLayout(overlayView, params)`

