package edu.upc.blopup.vitalsform.model

open class Routes(val id:String) {
    object BloodPressureScreen: Routes("Blood Pressure Screen with AppBar")
    object HowToActivateBluetoothScreen: Routes("How to activate Bluetooth Screen")
    object BloodPressureDataScreen: Routes("Blood Pressure Data Screen")
}