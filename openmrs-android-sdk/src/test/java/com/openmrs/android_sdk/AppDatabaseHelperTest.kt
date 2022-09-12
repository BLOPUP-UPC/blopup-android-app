package com.openmrs.android_sdk

import com.openmrs.android_sdk.library.databases.AppDatabaseHelper
import com.openmrs.android_sdk.library.databases.entities.DiagnosisEntity
import com.openmrs.android_sdk.library.models.Diagnosis
import com.openmrs.android_sdk.library.models.Link
import org.junit.Assert.*

import org.junit.Test

class AppDatabaseHelperTest {

    @Test
    fun convert() {

        // Given
        val link = Link()
        link.rel = "myRel"
        link.uri = "myUri"
        val diagnosis = Diagnosis("uuid", "display", listOf(link), 1L)

        val encounterId = 5L

        // When
        val result: DiagnosisEntity = AppDatabaseHelper.convert(diagnosis, encounterId)

        // Then
        assertEquals("uuid", result.uuid)
        assertEquals(listOf(link), result.links)
        assertEquals("display", result.display)
        assertEquals(encounterId, result.encounterId)
    }
}