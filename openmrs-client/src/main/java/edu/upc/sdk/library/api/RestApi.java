/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package edu.upc.sdk.library.api;

import static edu.upc.sdk.library.api.repository.VisitRepository.VISIT_API_REPRESENTATION;

import androidx.annotation.Keep;

import java.util.Map;

import edu.upc.sdk.library.api.repository.DoctorRepository.ContactDoctorRequest;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.Encountercreate;
import edu.upc.sdk.library.models.IdGenPatientIdentifiers;
import edu.upc.sdk.library.models.IdentifierType;
import edu.upc.sdk.library.models.LegalConsentRequest;
import edu.upc.sdk.library.models.Obscreate;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.OpenMRSVisit;
import edu.upc.sdk.library.models.PatientDto;
import edu.upc.sdk.library.models.PatientDtoUpdate;
import edu.upc.sdk.library.models.Provider;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.Session;
import edu.upc.sdk.library.models.User;
import edu.upc.sdk.library.models.VisitType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * The interface Rest api.
 */
@Keep
public interface RestApi {

    /**
     * Gets locations.
     *
     * @param representation the representation
     * @return the locations
     */
    @GET("location?tag=Login%20Location")
    Call<Results<LocationEntity>> getLocations(@Query("v") String representation);

    /**
     * Gets locations.
     *
     * @param url            the url
     * @param tag            the tag
     * @param representation the representation
     * @return the locations
     */
    @GET()
    Call<Results<LocationEntity>> getLocations(@Url String url,
                                               @Query("tag") String tag,
                                               @Query("v") String representation);

    /**
     * Gets identifier types.
     *
     * @return the identifier types
     */
    @GET("patientidentifiertype")
    Call<Results<IdentifierType>> getIdentifierTypes();

    /**
     * Gets patient identifiers.
     *
     * @param username the username
     * @param password the password
     * @return the patient identifiers
     */
    @GET("module/idgen/generateIdentifier.form?source=1")
    Call<IdGenPatientIdentifiers> getPatientIdentifiers(@Query("username") String username,
                                                        @Query("password") String password);

    /**
     * Gets patient by uuid.
     *
     * @param uuid           the uuid
     * @param representation the representation
     * @return the patient by uuid
     */
    @GET("patient/{uuid}")
    Call<PatientDto> getPatientByUUID(@Path("uuid") String uuid,
                                      @Query("v") String representation);

    /**
     * Create patient call.
     *
     * @param patientDto the patient dto
     * @return the call
     */
    @POST("patient")
    Call<PatientDto> createPatient(@Body PatientDto patientDto);

    /**
     * Gets patients.
     *
     * @param searchQuery    the search query
     * @param representation the representation
     * @return the patients
     */

    @GET("patient")
    Call<Results<PatientDto>> getPatientsDto(@Query("q") String searchQuery,
                                             @Query("v") String representation);

    @POST("upload")
    Call<String> uploadLegalConsent(@Body LegalConsentRequest legalConsentRequest);

    /**
     * Create obs call.
     *
     * @param obscreate the obscreate
     * @return the call
     */
    @POST("obs")
    Call<Observation> createObs(@Body Obscreate obscreate);

    /**
     * Update obs call.
     *
     * @param uuid the uuid of the observation to be updated
     * @param value the updated observation data
     * @return the call
     */
    @POST("obs/{uuid}")
    Call<Observation> updateObservation(@Path("uuid") String uuid,
                                        @Body Map<String, Object> value);

    /**
     * Get obs call.
     *
     * @param uuid the uuid of the observation to be updated
     * @return an observation
     */
    @GET("obs/{uuid}")
    Call<Observation> getObservationByUuid(@Path("uuid") String uuid);

    /**
     * Create encounter call.
     *
     * @param encountercreate the encountercreate
     * @return the call
     */
    @POST("encounter")
    Call<Encounter> createEncounter(@Body Encountercreate encountercreate);

    /**
     * Gets session.
     *
     * @return the session
     */

    /**
     * Update obs call.
     *
     * @param uuid the uuid of the encounter to be removed
     * @return the call
     */
    @DELETE("encounter/{uuid}")
    Call<ResponseBody> deleteEncounter(@Path("uuid") String uuid);

    @GET("session")
    Call<Session> getSession();

    /**
     * Ends a visit by its uuid.
     *
     * @param uuid              the visit uuid to be ended
     * @param visitWithStopDate An empty visit containing the stop date and time
     * @return the call
     */
    @POST("visit/{uuid}")
    Call<OpenMRSVisit> endVisitByUUID(@Path("uuid") String uuid, @Body OpenMRSVisit visitWithStopDate);

    /**
     * Start visit call.
     *
     * @param visit the visit
     * @return the call
     */
    @POST("visit")
    Call<OpenMRSVisit> startVisit(@Body OpenMRSVisit visit);

    /**
     * Find visits by patient uuid call.
     *
     * @param patientUUID    the patient uuid
     * @param representation the representation
     * @return the call
     */
    @GET("visit")
    Call<Results<OpenMRSVisit>> findVisitsByPatientUUID(@Query("patient") String patientUUID,
                                                        @Query("v") String representation);

    /**
     * Find active visits.
     *
     * @param patientUUID the patient uuid
     * @return the call
     */
    @GET("visit?v=" + VISIT_API_REPRESENTATION + "&includeInactive=false")
    Call<Results<OpenMRSVisit>> findActiveVisitsByPatientUUID(@Query("patient") String patientUUID);

    /**
     * Gets visit type.
     *
     * @return the visit type
     */
    @GET("visittype")
    Call<Results<VisitType>> getVisitType();


    /**
     * Get visit call
     *
     * @param uuid the uuid of the visit
     * @return a visit
     */
    @GET("visit/{uuid}?v=" + VISIT_API_REPRESENTATION)
    Call<OpenMRSVisit> getVisitByUuid(@Path("uuid") String uuid);

    /**
     * Delete provider call.
     *
     * @param uuid the uuid
     * @return the call
     */
    @DELETE("visit/{uuid}")
    Call<ResponseBody> deleteVisit(@Path("uuid") String uuid);


    /**
     * Update patient call.
     *
     * @param patientDto     the patient dto
     * @param uuid           the uuid
     * @param representation the representation
     * @return the call
     */
    @POST("patient/{uuid}")
    Call<PatientDto> updatePatient(@Body PatientDtoUpdate patientDto, @Path("uuid") String uuid,
                                   @Query("v") String representation);

    /**
     * Gets user info.
     *
     * @param username the username
     * @return the user info
     */
    @GET("user")
    Call<Results<User>> getUserInfo(@Query("q") String username);

    /**
     * Gets full user info.
     *
     * @param uuid the uuid
     * @return the full user info
     */
    @GET("user/{uuid}")
    Call<User> getFullUserInfo(@Path("uuid") String uuid);

    /**
     * Gets provider list.
     *
     * @return the provider list
     */
    @GET("provider?v=full")
    Call<Results<Provider>> getProviderList();

    /**
     * Gets provider attributes
     *
     * @return the provider list
     */
    @GET("provider/{uuid}/attribute")
    Call<Results<Provider>> getProviderAttributes(@Path("uuid") String uuid);

    /**
     * Add contact doctor call.
     *
     * @param contactDoctorRequest to Blopup
     * @return the call
     */
    @POST("contactDoctor")
    Call<ResponseBody> contactDoctor(@Body ContactDoctorRequest contactDoctorRequest);

}
