# FloatingButton
little JetPack compose app with the minimum features required to show and hide a button over the apps

### Project status : in contruction... (nothing implemented)

## target audience
This project if for Jetpack Compose initiated user

## Presentation
The goal of this demo is to explain the way to show and hide a composable (button) over all the apps. In order to make the app the more simple possible, the button wont be draggable.

## Warning
- 1 permission required : SYSTEM_ALERT_WINDOW (display over other apps)
- 1 service needs to be created : In order to be able to use the other apps when the button is workable
- 1 dependency needed "androidx.savedstate" : as the services don't have state management like activities or fragments, we need to use this library in order to recover the states from the ui created (ComposeView) in case the service is stopped or restarted.

# Init


