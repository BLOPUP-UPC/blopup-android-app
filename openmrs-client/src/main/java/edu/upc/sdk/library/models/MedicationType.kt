package edu.upc.sdk.library.models

import android.content.Context
import edu.upc.R

enum class MedicationType(val conceptId: String, val label: Int) {
    ACE_INHIBITOR("467f7d87-8c2e-4519-9e81-048c2c7824fd", R.string.ace_inhibitor),
    DIURETIC("a7fa1f5f-1ca3-4fe4-b02b-bd1dcc90201b", R.string.diuretic),
    CALCIUM_CHANNEL_BLOCKER("2146fbb8-8a8a-44f5-81de-2bee8ec4edce", R.string.calcium_channel_blocker),
    BETA_BLOCKER("f2c7ec86-6fe0-4e6a-bfe9-c73380228177", R.string.beta_blocker),
    ARA_II("87e51329-cc96-426d-bc71-ccef8892ce71", R.string.angiotensin_receptor_blocker);

    fun getLabel(context: Context) = context.getString(this.label)
}
