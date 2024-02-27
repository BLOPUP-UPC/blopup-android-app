package edu.upc.blopup.toggles

import edu.upc.BuildConfig

val hardcodeBluetoothDataToggle = ReleaseToggle(BuildConfigWrapper.hardcodeBluetoothDataToggle)
val contactDoctorToggle = ReleaseToggle(BuildConfigWrapper.contactDoctorToggle)
val newVitalsFlowToggle = ReleaseToggle(BuildConfigWrapper.newVitalsFlowToggle)

typealias OnToggleEnabled = () -> Unit
typealias OnToggleDisabled = () -> Unit

data class ReleaseToggle(val enabled: Boolean = false)

fun ReleaseToggle.check(
    onToggleEnabled: OnToggleEnabled? = null,
    onToggleDisabled: OnToggleDisabled? = null
) = if (this.enabled) onToggleEnabled?.invoke() else onToggleDisabled?.invoke()

//object to be able to mock BuildConfig in tests.
object BuildConfigWrapper {
    val hardcodeBluetoothDataToggle: Boolean
        get() = BuildConfig.HARDCODE_BLUETOOTH_DATA_TOGGLE

    val contactDoctorToggle: Boolean
        get() = BuildConfig.CONTACT_DOCTOR_TOGGLE

    val newVitalsFlowToggle: Boolean
        get() = BuildConfig.NEW_VITALS_FLOW
}
