package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.databases.AppDatabaseHelper
import edu.upc.sdk.library.databases.entities.FormResourceEntity
import rx.Observable
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormRepository @Inject constructor() : BaseRepository(null) {

    /**
     * Fetches a resource form by the form's name.
     *
     * @param name the form name
     * @return an observable form resource entity
     */
    fun fetchFormResourceByName(name: String): Observable<FormResourceEntity> {
        return AppDatabaseHelper.createObservableIO(Callable {
            return@Callable db.formResourceDAO().getFormResourceByName(name)
        })
    }
}
