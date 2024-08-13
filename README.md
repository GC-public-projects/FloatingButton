# FloatingButton
little JetPack compose app with the minimum features required to show and hide a button over the apps

### Project status : Workable, documentation in progress...

## target audience
This project if for Jetpack Compose initiated user

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

## MainActivity (class)

### Purpose
- call the alert dialog to sh






