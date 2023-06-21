package edu.upc.sdk

import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import edu.upc.openmrs.utilities.FileUtils
import edu.upc.sdk.library.OpenmrsAndroid
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.dao.LegalConsentDAO
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.ResultType
import io.mockk.every
import io.mockk.mockkStatic
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import rx.Observable.*
import java.io.File
import java.util.*


@RunWith(RobolectricTestRunner::class)
class RecordingRepositoryTest {
    lateinit var testFilePath: String
    private lateinit var legalConsent: LegalConsent
    lateinit var recordingRepository: RecordingRepository
    lateinit var mockServer: MockWebServer


    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())

        testFilePath = FileUtils.getRecordingFilePath()
        File(testFilePath).createNewFile()

        legalConsent = LegalConsent().apply {
            filePath = testFilePath
            patientIdentifier = UUID.randomUUID().toString()
        }
        mockServer = MockWebServer()

        val port = mockServer.port
        val baseURL = "http://localhost:$port/"
        mockkStatic(OpenmrsAndroid::class)
        every { OpenmrsAndroid.getServerUrl() } returns baseURL

        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("\"response\": \"Ok\""))
        mockServer.enqueue(  MockResponse().setResponseCode(503).setBody("\"response\": \"Service Unavailable\""))

        recordingRepository = RecordingRepository(LegalConsentDAO())
    }

    @Test
    internal fun `should return RecordingSuccess when call to fileUpload  is successful`() {
        val result = recordingRepository.saveRecording(legalConsent)
        val actual = result.toBlocking().first()

        Assertions.assertEquals(ResultType.RecordingSuccess, actual)
    }

    @Test
    internal fun `should return RecordingError when call to fileUpload  fails`() {
        val result = recordingRepository.saveRecording(legalConsent)
        val actual = result.toBlocking().first()

        Assertions.assertEquals(ResultType.RecordingError, actual)
    }
}
