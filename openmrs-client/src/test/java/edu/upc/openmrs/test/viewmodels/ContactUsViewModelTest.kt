package edu.upc.openmrs.test.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.openmrs.android_sdk.library.api.repository.EmailRepository
import com.openmrs.android_sdk.library.models.EmailRequest
import edu.upc.openmrs.activities.community.contact.ContactUsViewModel
import edu.upc.openmrs.test.ACUnitTestBaseRx
import org.junit.Before
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

    @Before
    override fun setUp () {
        super.setUp()
    }

    @Test
    fun whenSendARequest_thenCallToTheAPI() {
        val contactUsViewModel = ContactUsViewModel(emailRepository)
        val emailRequest = createEmailRequest()

        `when`(emailRepository.sendEmail(emailRequest)).thenReturn(Observable.just("Success"))
        contactUsViewModel.sendEmail(emailRequest)

        verify(emailRepository).sendEmail(any())
    }

    private fun createEmailRequest() : EmailRequest {
        return EmailRequest("Subject", "Content of email")
    }
}