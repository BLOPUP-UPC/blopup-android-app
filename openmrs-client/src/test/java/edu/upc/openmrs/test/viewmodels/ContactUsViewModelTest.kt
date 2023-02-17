package edu.upc.openmrs.test.viewmodels

import com.openmrs.android_sdk.library.api.repository.EmailRepository
import com.openmrs.android_sdk.library.models.EmailRequest
import edu.upc.openmrs.activities.community.contact.ContactUsViewModel
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import rx.Observable

@RunWith(JUnit4::class)
class ContactUsViewModelTest {

    @Mock
    lateinit var emailRepository : EmailRepository

    @Before
    fun setUp () {
        MockitoAnnotations.initMocks(this)
    }

    @Ignore
    @Test
    fun whenSendARequest_thenCallToTheAPI() {
        val contactUsViewModel = ContactUsViewModel(emailRepository)
        val emailRequest = createEmailRequest()
        val sendEmailExpectedResult = true

        `when`(emailRepository.sendEmail(emailRequest)).thenReturn(Observable.just(sendEmailExpectedResult))
        contactUsViewModel.sendEmail(emailRequest)

        verify(emailRepository).sendEmail(any())
    }

    private fun createEmailRequest() : EmailRequest {
        return EmailRequest("Subject", "Content of email")
    }
}