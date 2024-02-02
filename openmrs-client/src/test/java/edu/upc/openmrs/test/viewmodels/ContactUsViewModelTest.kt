package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import edu.upc.openmrs.activities.community.contact.ContactUsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import edu.upc.sdk.library.api.repository.EmailRepository
import edu.upc.sdk.library.models.EmailRequest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import rx.Observable

@RunWith(JUnit4::class)
class ContactUsViewModelTest : ACUnitTestBaseRx() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var emailRepository : EmailRepository

    private val emailRequest = createEmailRequest()

    @Test
    fun whenSendARequest_thenCallToTheAPI() {
        val contactUsViewModel = ContactUsViewModel(emailRepository)

        `when`(emailRepository.sendEmail(emailRequest)).thenReturn(Observable.just("Success"))
        contactUsViewModel.sendEmail(emailRequest)

        verify(emailRepository).sendEmail(any())
    }

    @Test
    fun whenSendEmailIsNotPossible_thenThrowAnError() {
        val contactUsViewModel = ContactUsViewModel(emailRepository)
        val errorMsg = "EmailSentError"
        val exception = Exception(errorMsg)

        `when`(emailRepository.sendEmail(emailRequest)).thenReturn(Observable.error(exception))

        val actualResult = contactUsViewModel.sendEmail(emailRequest)

        assertEquals(errorMsg, actualResult.value.toString())
    }

    private fun createEmailRequest() : EmailRequest {
        return EmailRequest("Subject", "Content of email")
    }
}