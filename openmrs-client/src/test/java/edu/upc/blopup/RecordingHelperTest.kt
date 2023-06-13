package edu.upc.blopup

import edu.upc.openmrs.utilities.FileUtils
import edu.upc.sdk.library.api.repository.RecordingRepository
import edu.upc.sdk.library.models.LegalConsent
import edu.upc.sdk.library.models.ResultType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import rx.Observable
import java.util.*

class RecordingHelperTest {

    private lateinit var legalConsent: LegalConsent
    private lateinit var recordingHelper: RecordingHelper
    private lateinit var recordingRepository: RecordingRepository
    private lateinit var fullFilePath: String

    @Before
    fun setUp() {
        legalConsent = LegalConsent().apply {
            filePath = FILE_NAME
            patientIdentifier = UUID.randomUUID().toString()
        }

        recordingRepository = mockk()
        every { recordingRepository.saveRecording(legalConsent) } returns Observable.just(ResultType.RecordingSuccess)

        recordingHelper = RecordingHelper(recordingRepository)

        mockkStatic(FileUtils::class)

        every { FileUtils.getRootDirectory() } returns ROOT_DIRECTORY

        fullFilePath = "$ROOT_DIRECTORY/$FILE_NAME"
    }

    @Test
    fun `should save legalConsent to Repository`() {
        recordingHelper.saveLegalConsent(legalConsent)

        verify { recordingRepository.saveRecording(legalConsent) }
    }

    @Test
    fun `should remove recording file from local when successful`() {
        recordingHelper.saveLegalConsent(legalConsent)

        verify { FileUtils.removeLocalRecordingFile(legalConsent.filePath!!) }
    }

    companion object {
        private const val FILE_NAME = "20230525_123926_.mp3"
        private const val ROOT_DIRECTORY = "/storage/emulated/0/Android/data/edu.upc/files/DCIM"
    }
}
