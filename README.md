# BlopUp Android app

## Description
This is the Android app that is used to access the BlopUp platform.

BLOPUP is a medical device to control the blood pressure. 
It allows to measure the blood pressure connecting to the blood pressure device by bluetooth and give recommendations after receiving the data.
The app also allows to weight the patient, to measure the height and to calculate the BMI. You can search for all the measurements and see the history of the patient.

## Installation

1) Clone the repository.
2) Open the project in Android Studio. CLick on "Open an existing Android Studio project" and select the folder where you cloned the repository.
3) Dependencies will be downloaded automatically.
4) Configure emulator or device. You can use the emulator that comes with Android Studio or connect a physical device. Ensure that the device/emulator is properly configured and running.
5) Build and run the project. Click on the play button in Android Studio with the emulator or device selected.
6) Android Studio will compile the project and deploy the app to the device.
7) Explore the app. Once the app is installed and running, explore its features and functionalities. Use the app to access the BlopUp platform and perform various tasks.


## Development

### Project History
The BlopUp Android app project began as a fork from the OpenMRS Android app repository, which provided a solid foundation for building upon.
The project has been modified and extended to meet the requirements of the BlopUp platform.

### Transition to Kotlin
To leverage the benefits of modern Android development, we have been gradually transitioning the codebase from Java to Kotlin. Kotlin's concise syntax, null safety, and interoperability with Java have significantly enhanced the development experience and code quality. 
You will find a mix of Java and Kotlin files in the project, specially the legacy code from OpenMRS where you will find lots of Java files. All new code should be written in Kotlin.

### Adoption of Jetpack Compose
We are currently in the midst of migrating to Jetpack Compose, Google's modern toolkit for building native Android UI. 
This transition aims to improve UI development efficiency, simplify UI testing, and provide a more declarative and reactive approach to building user interfaces.
The files migrating up to now are in the `ui` package, under Main Activity (Dashboard, Patient Search, Create a patien and the whole flow of taking the measurements from a patient).
Other sections of the app still utilize traditional XML-based layouts.

### Android Architecture

## Connect to local OpenMRS
The BlopUp Android app follows the Model-View-ViewModel (MVVM) architecture pattern, which promotes separation of concerns and maintainability:
1) Model: Represents the data and business logic of the application. It encapsulates the data sources, such as databases or network requests, and provides an interface for accessing and manipulating the data.
2) View: Represents the user interface (UI) components of the application. It is responsible for displaying data to the user and capturing user interactions. In MVVM, the view is passive and delegates UI-related logic to the ViewModel.
3) ViewModel: Acts as a mediator between the view and the model. It contains the presentation logic and exposes data to the view through observable LiveData or StateFlows. ViewModels survive configuration changes and are responsible for handling user interactions, business logic, and data manipulation.

While the project primarily adheres to the MVVM architecture, you may encounter legacy code that utilizes the Presenter pattern. 
Presenters were commonly used in Android development before the widespread adoption of MVVM.

Use http://10.0.2.2/openmrs as the server url

### E2E tests
We use maestro for the e2e tests. They can be executed locally and are executed in the CI automatically.
Tests are stored in the .maestro folder.

#### Locally:
```
curl -Ls "https://get.maestro.mobile.dev" | bash # Install Maestro cli
maestro test # Run all tests (will detect any emulator or device connected, the app should be installed)
maestro studio # Start Maestro studio for writing tests
``` 

#### CI:
The tests are executed in the maestro cloud servers. You can go to the dashboard to check the results.

# Release

With every tag the CI will build a release and upload it to the Play Console

