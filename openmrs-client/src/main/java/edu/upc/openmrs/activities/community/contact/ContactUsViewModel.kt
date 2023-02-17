package edu.upc.openmrs.activities.community.contact

import com.openmrs.android_sdk.library.api.repository.EmailRepository
import com.openmrs.android_sdk.library.models.EmailRequest
import com.openmrs.android_sdk.library.models.OperationType
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.activities.BaseViewModel
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class ContactUsViewModel @Inject constructor(
    private val emailRepository: EmailRepository
) : BaseViewModel<Boolean>() {

     fun sendEmail(emailRequest: EmailRequest) {
        //setLoading(OperationType.EmailFetching)
        addSubscription(emailRepository.sendEmail(emailRequest)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { setContent(it, OperationType.EmailFetching) },
                { setError (it, OperationType.EmailFetching) }
            )
        )
    }
}