package edu.upc.blopup.toggles

import edu.upc.BuildConfig

val hardcodeBluetoothDataToggle = ReleaseToggle(BuildConfig.HARDCODE_BLUETOOTH_DATA_TOGGLE)
val contactDoctorToggle = ReleaseToggle(BuildConfig.CONTACT_DOCTOR_TOGGLE)

typealias OnToggleEnabled = () -> Unit
typealias OnToggleDisabled = () -> Unit

data class ReleaseToggle(val enabled: Boolean = false)

fun ReleaseToggle.check(
    onToggleEnabled: OnToggleEnabled? = null,
    onToggleDisabled: OnToggleDisabled? = null
) = if (this.enabled) onToggleEnabled?.invoke() else onToggleDisabled?.invoke()
