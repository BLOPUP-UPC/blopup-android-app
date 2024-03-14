# BlopUp Android app

## Description
This is the Android app that is used to access the BlopUp platform.

## Development

## Connect to local OpenMRS

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

