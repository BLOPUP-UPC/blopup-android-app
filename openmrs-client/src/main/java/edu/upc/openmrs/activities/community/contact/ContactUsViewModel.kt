package edu.upc.openmrs.activities.community.contact

import androidx.lifecycle.LiveData
import com.openmrs.android_sdk.library.api.repository.EmailRepository
import com.openmrs.android_sdk.library.models.EmailRequest
import com.openmrs.android_sdk.library.models.OperationType
import com.openmrs.android_sdk.library.models.Result
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