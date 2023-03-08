package edu.upc.openmrs.activities.community.contact

import androidx.lifecycle.LiveData
import edu.upc.sdk.library.api.repository.EmailRepository
import edu.upc.sdk.library.models.EmailRequest
import edu.upc.sdk.library.models.OperationType
import edu.upc.sdk.library.models.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class ContactUsViewModel @Inject constructor(
    private val emailRepository: EmailRepository
) : BaseViewModel<Unit>() {

     fun sendEmail(emailRequest: EmailRequest) : LiveData<Result<Unit>> {
        addSubscription(emailRepository.sendEmail(emailRequest)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { setContent(Unit, OperationType.EmailSent) },
                { setError (it, OperationType.EmailSent) }
            )
        )
         return result;
    }
}