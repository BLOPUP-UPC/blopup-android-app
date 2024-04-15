package edu.upc.blopup.ui

import edu.upc.sdk.utilities.ApplicationConstants

open class Routes(val id: String) {
    object MeasureBloodPressureScreen : Routes("Measure Blood Pressure Screen")
    object HowToActivateBluetoothScreen : Routes("How to activate Bluetooth Screen")
    object BloodPressureDataScreen : Routes("Blood Pressure Data Screen")
    object MeasureWeightScreen : Routes("Measure Weight Data Screen")
    object WeightDataScreen : Routes("Weight Data Screen")
    object MeasureHeightScreen : Routes("Measure Height Data Screen")
    object TreatmentAdherenceScreen : Routes("Treatment Adherence Data Screen")
    object DashboardScreen : Routes("Dashboard Screen")
    object CreatePatientScreen : Routes("Create Patient Screen")
    object PatientDashboardScreen : Routes("PatientDashboardScreen/{${ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE}}/{${ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE}}")
}