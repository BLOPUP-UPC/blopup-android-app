package edu.upc.blopup.toggles

import edu.upc.BuildConfig

val hardcodeBluetoothDataToggle = ReleaseToggle { BuildConfigWrapper.hardcodeBluetoothDataToggle }

typealias OnToggleEnabled = () -> Unit
typealias OnToggleDisabled = () -> Unit

data class ReleaseToggle(val enabled: () -> Boolean = { false })

fun ReleaseToggle.check(
    onToggleEnabled: OnToggleEnabled? = null,
    onToggleDisabled: OnToggleDisabled? = null
) = if (this.enabled()) onToggleEnabled?.invoke() else onToggleDisabled?.invoke()

//object to be able to mock BuildConfig in tests.
object BuildConfigWrapper {
    val hardcodeBluetoothDataToggle: Boolean
        get() = BuildConfig.HARDCODE_BLUETOOTH_DATA_TOGGLE
}
