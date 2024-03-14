package edu.upc.blopup.ui

open class Routes(val id: String) {
    object MeasureBloodPressureRoute : Routes("Measure Blood Pressure Screen")
    object HowToActivateBluetoothScreen : Routes("How to activate Bluetooth Screen")
    object BloodPressureRoute : Routes("Blood Pressure Data Screen")
    object MeasureWeightScreen : Routes("Measure Weight Data Screen")
    object WeightDataScreen : Routes("Weight Data Screen")
    object MeasureHeightScreen : Routes("Measure Height Data Screen")
    object TreatmentAdherenceScreen : Routes("Treatment Adherence Data Screen")
}