package edu.upc.sdk.library.api

enum class ObservationConcept (val display: String, val uuid: String){
    MEDICATION_NAME("Medication Name", "a721776b-fd0f-41ea-821b-0d0df94d5560"),
    RECOMMENDED_BY("Recommended By", "c1164da7-0b4f-490f-85da-0c4aac4ca8a1"),
    ACTIVE("Active", "81f60010-961e-4bc5-aa04-435c7ace1ee3"),
    TREATMENT_NOTES("Treatment Notes", "dfa881a4-5c88-4057-958b-f583c8edbdef"),
    MEDICATION_TYPE("Medication Type", "1a8f49cc-488b-4788-adb3-72c499108772"),
    TREATMENT_ADHERENCE("Treatment Adherence", "87e51329-cc96-426d-bc71-ccef8892ce72")
}