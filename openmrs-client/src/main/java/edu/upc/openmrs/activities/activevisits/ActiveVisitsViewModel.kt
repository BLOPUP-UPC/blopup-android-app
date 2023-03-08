package edu.upc.openmrs.activities.activevisits

import edu.upc.sdk.library.dao.VisitDAO
import edu.upc.sdk.library.models.OperationType.ActiveVisitsFetching
import edu.upc.sdk.library.models.OperationType.ActiveVisitsSearching
import edu.upc.sdk.library.models.Visit
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.upc.openmrs.utilities.FilterUtil
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

@HiltViewModel
class ActiveVisitsViewModel @Inject constructor(private val visitDAO: VisitDAO) : edu.upc.openmrs.activities.BaseViewModel<List<Visit>>() {

    fun fetchActiveVisits() {
        setLoading()
        addSubscription(visitDAO.activeVisits
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { visits: List<Visit> -> setContent(visits) },
                        { setError(it, ActiveVisitsFetching) }
                ))
    }

    fun fetchActiveVisits(query: String) {
        setLoading()
        addSubscription(visitDAO.activeVisits
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { visits: List<Visit> ->
                            val filteredVisits = FilterUtil.getPatientsWithActiveVisitsFilteredByQuery(visits, query)
                            setContent(filteredVisits)
                        },
                        { setError(it, ActiveVisitsSearching) }
                ))
    }
}
