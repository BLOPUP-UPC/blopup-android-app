package edu.upc.blopup.ui

open class Routes(val id: String) {
    object MeasureBloodPressureScreen : Routes("Measure Blood Pressure Screen")
    object HowToActivateBluetoothScreen : Routes("How to activate Bluetooth Screen")
    object BloodPressureDataScreen : Routes("Blood Pressure Data Screen")
    object MeasureWeightScreen : Routes("Measure Weight Data Screen")
    object WeightDataScreen : Routes("Weight Data Screen")
    object MeasureHeightScreen : Routes("Measure Height Data Screen")
    object TreatmentAdherenceScreen : Routes("Treatment Adherence Data Screen")
    object DashboardScreen : Routes("Dashboard Screen")
    object AddEditPatientScreen : Routes("Add Edit Patient Screen")
}