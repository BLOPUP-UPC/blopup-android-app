package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.models.Treatment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreatmentRepository @Inject constructor() : BaseRepository(null) {
    fun saveTreatment(treatment: Treatment) {

    }

}

object ConceptIds{

    const val MEDICATION_NAME_CONCEPT_ID = "43f8a8f3-6cf9-4a1f-bba5-8ec979f6d0b6"
    const val RECOMMENDED_BY_CONCEPT_ID = "c1164da7-0b4f-490f-85da-0c4aac4ca8a1"
    const val ACTIVE_CONCEPT_ID = "81f60010-961e-4bc5-aa04-435c7ace1ee3"
    const val TREATMENT_NOTES_CONCEPT_ID = "dfa881a4-5c88-4057-958b-f583c8edbdef"
    const val DRUG_FAMILIES_CONCEPT_ID = "1a8f49cc-488b-4788-adb3-72c499108772"

    const val BETA_BLOCKERS_CONCEPT_ID = "f2c7ec86-6fe0-4e6a-bfe9-c73380228177"
    const val ACE_INHIBITORS_CONCEPT_ID = "126205BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
    const val ANGIOTENSIS_RECEPTOR_BLOCKERS_CONCEPT_ID = "125412BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"
    const val CALCIUM_CHANNEL_BLOCKERS_CONCEPT_ID = "2146fbb8-8a8a-44f5-81de-2bee8ec4edce"
    const val DIURETICS_CONCEPT_ID = "a7fa1f5f-1ca3-4fe4-b02b-bd1dcc90201b"
}
