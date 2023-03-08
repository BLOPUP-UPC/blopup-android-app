package edu.upc.sdk.library.api.repository

import edu.upc.sdk.library.databases.AppDatabaseHelper
import edu.upc.sdk.library.databases.entities.FormResourceEntity
import edu.upc.sdk.library.models.FormData
import rx.Observable
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormRepository @Inject constructor() : BaseRepository() {

    /**
     * Fetches forms as a list of resources.
     *
     * @return observable list of form resources
     */
    fun fetchFormResourceList(): Observable<List<FormResourceEntity>> {
        return AppDatabaseHelper.createObservableIO(Callable {
            return@Callable db.formResourceDAO().getFormResourceList()
        })
    }

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

    /**
     * Creates a form.
     *
     * @param uuid UUID of the form resource
     * @param formData form data that will be created
     * @return observable boolean true if operation is successful
     */
    fun createForm(uuid: String, formData: FormData): Observable<Boolean> {
        return AppDatabaseHelper.createObservableIO(Callable {
            restApi.formCreate(uuid, formData).execute().run {
                if (isSuccessful && body()!!.name == "json") return@run true
                else throw Exception("Error creating forms: ${message()}")
            }
        })
    }
}
