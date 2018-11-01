# AndroidBarcodeScanner

### Functionality 

Android app to read barcodes using [`CameraKit`](https://github.com/CameraKit) and [Firebase ML Kit](https://firebase.google.com/docs/ml-kit/android/read-barcodes)

### Arhitecture

Very simple app with only one screen where I tried to implement a very simple unidirectional approach. The screen logic is controlled by a `ViewModel`that is in charge of changing the `State` and exposing it to the view. The `View` has two jobs: rendering the states that receives from the `ViewModel`via a `LiveData` and notify the `ViewModel` when there is an event.


