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

import edu.upc.sdk.library.databases.entities.ConceptEntity;
import edu.upc.sdk.library.databases.entities.FormResourceEntity;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.models.Allergy;
import edu.upc.sdk.library.models.AllergyCreate;
import edu.upc.sdk.library.models.ConceptAnswers;
import edu.upc.sdk.library.models.ConceptMembers;
import edu.upc.sdk.library.models.EmailRequest;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.EncounterType;
import edu.upc.sdk.library.models.Encountercreate;
import edu.upc.sdk.library.models.FormCreate;
import edu.upc.sdk.library.models.FormData;
import edu.upc.sdk.library.models.IdGenPatientIdentifiers;
import edu.upc.sdk.library.models.IdentifierType;
import edu.upc.sdk.library.models.Module;
import edu.upc.sdk.library.models.Obscreate;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.PatientDto;
import edu.upc.sdk.library.models.PatientDtoUpdate;
import edu.upc.sdk.library.models.PatientPhoto;
import edu.upc.sdk.library.models.Provider;
import edu.upc.sdk.library.models.Resource;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.Session;
import edu.upc.sdk.library.models.SystemProperty;
import edu.upc.sdk.library.models.SystemSetting;
import edu.upc.sdk.library.models.User;
import edu.upc.sdk.library.models.Visit;
import edu.upc.sdk.library.models.VisitType;

import java.util.Map;

import edu.upc.sdk.library.databases.entities.ConceptEntity;
import edu.upc.sdk.library.databases.entities.FormResourceEntity;
import edu.upc.sdk.library.databases.entities.LocationEntity;
import edu.upc.sdk.library.models.Allergy;
import edu.upc.sdk.library.models.AllergyCreate;
import edu.upc.sdk.library.models.ConceptAnswers;
import edu.upc.sdk.library.models.ConceptMembers;
import edu.upc.sdk.library.models.EmailRequest;
import edu.upc.sdk.library.models.Encounter;
import edu.upc.sdk.library.models.EncounterType;
import edu.upc.sdk.library.models.Encountercreate;
import edu.upc.sdk.library.models.FormCreate;
import edu.upc.sdk.library.models.FormData;
import edu.upc.sdk.library.models.IdGenPatientIdentifiers;
import edu.upc.sdk.library.models.IdentifierType;
import edu.upc.sdk.library.models.Module;
import edu.upc.sdk.library.models.Obscreate;
import edu.upc.sdk.library.models.Observation;
import edu.upc.sdk.library.models.Patient;
import edu.upc.sdk.library.models.PatientDto;
import edu.upc.sdk.library.models.PatientDtoUpdate;
import edu.upc.sdk.library.models.PatientPhoto;
import edu.upc.sdk.library.models.Provider;
import edu.upc.sdk.library.models.Resource;
import edu.upc.sdk.library.models.Results;
import edu.upc.sdk.library.models.Session;
import edu.upc.sdk.library.models.SystemProperty;
import edu.upc.sdk.library.models.SystemSetting;
import edu.upc.sdk.library.models.User;
import edu.upc.sdk.library.models.Visit;
import edu.upc.sdk.library.models.VisitType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * The interface Rest api.
 */
public interface RestApi {
    /**
     * Gets forms.
     *
     * @return the forms
     */
    @GET("form?v=custom:(uuid,name,resources)")
    Call<Results<FormResourceEntity>> getForms();

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
     * Gets system property.
     *
     * @param property       the property
     * @param representation the representation
     * @return the system property
     */
    @GET("systemsetting")
    Call<Results<SystemProperty>> getSystemProperty(@Query("q") String property,
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
     * Gets last viewed patients.
     *
     * @param limit      the limit
     * @param startIndex the start index
     * @return the last viewed patients
     */
    @GET("patient?lastviewed&v=full")
    Call<Results<Patient>> getLastViewedPatients(@Query("limit") Integer limit,
                                                 @Query("startIndex") Integer startIndex);

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
    Call<Results<Patient>> getPatients(@Query("q") String searchQuery,
                                       @Query("v") String representation);

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

    /**
     * Upload patient photo call.
     *
     * @param uuid         the uuid
     * @param patientPhoto the patient photo
     * @return the call
     */
    @POST("personimage/{uuid}")
    Call<PatientPhoto> uploadPatientPhoto(@Path("uuid") String uuid,
                                          @Body PatientPhoto patientPhoto);

    @Multipart
    @POST("/api/v1/upload/consent")
    Call<String> uploadConsent(@Part MultipartBody.Part file);

    /**
     * Download patient photo call.
     *
     * @param uuid the uuid
     * @return the call
     */
    @GET("personimage/{uuid}")
    Call<ResponseBody> downloadPatientPhoto(@Path("uuid") String uuid);

    /**
     * Gets similar patients.
     *
     * @param patientData the patient data
     * @return the similar patients
     */
    @GET("patient?matchSimilar=true&v=full")
    Call<Results<Patient>> getSimilarPatients(@QueryMap Map<String, String> patientData);

    /**
     * Create obs call.
     *
     * @param obscreate the obscreate
     * @return the call
     */
    @POST("obs")
    Call<Observation> createObs(@Body Obscreate obscreate);

    /**
     * Create encounter call.
     *
     * @param encountercreate the encountercreate
     * @return the call
     */
    @POST("encounter")
    Call<Encounter> createEncounter(@Body Encountercreate encountercreate);

    /**
     * Gets encounter types.
     *
     * @return the encounter types
     */
    @GET("encountertype")
    Call<Results<EncounterType>> getEncounterTypes();

    /**
     * Gets encounter roles.
     *
     * @return the encounter roles
     */
    @GET("encounterrole")
    Call<Results<Resource>> getEncounterRoles();

    /**
     * Gets session.
     *
     * @return the session
     */
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
    Call<Visit> endVisitByUUID(@Path("uuid") String uuid, @Body Visit visitWithStopDate);

    /**
     * Start visit call.
     *
     * @param visit the visit
     * @return the call
     */
    @POST("visit")
    Call<Visit> startVisit(@Body Visit visit);

    /**
     * Find visits by patient uuid call.
     *
     * @param patientUUID    the patient uuid
     * @param representation the representation
     * @return the call
     */
    @GET("visit")
    Call<Results<Visit>> findVisitsByPatientUUID(@Query("patient") String patientUUID,
                                                 @Query("v") String representation);

    /**
     * Gets visit type.
     *
     * @return the visit type
     */
    @GET("visittype")
    Call<Results<VisitType>> getVisitType();

    /**
     * Gets last vitals.
     *
     * @param patientUUID    the patient uuid
     * @param encounterType  the encounter type
     * @param representation the representation
     * @param limit          the limit
     * @param order          the order
     * @return the last vitals
     */
    @GET("encounter")
    Call<Results<Encounter>> getLastVitals(@Query("patient") String patientUUID,
                                           @Query("encounterType") String encounterType,
                                           @Query("v") String representation,
                                           @Query("limit") int limit,
                                           @Query("order") String order);

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
     * Gets modules.
     *
     * @param representation the representation
     * @return the modules
     */
    @GET("module")
    Call<Results<Module>> getModules(@Query("v") String representation);

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
     * Gets concepts.
     *
     * @param limit      the limit
     * @param startIndex the start index
     * @return the concepts
     */
    @GET("concept")
    Call<Results<ConceptEntity>> getConcepts(@Query("limit") int limit, @Query("startIndex") int startIndex);

    /**
     * Gets concept from uuid.
     *
     * @param uuid the uuid
     * @return the concept from uuid
     */
    @GET("concept/{uuid}")
    Call<ConceptAnswers> getConceptFromUUID(@Path("uuid") String uuid);

    /**
     * Gets concept members from uuid.
     *
     * @param uuid the uuid
     * @return the concept members from uuid
     */
    @GET("concept/{uuid}")
    Call<ConceptMembers> getConceptMembersFromUUID(@Path("uuid") String uuid);

    /**
     * Gets system settings by query.
     *
     * @param query          the query
     * @param representation the representation
     * @return the system settings by query
     */
    @GET("systemsetting")
    Call<Results<SystemSetting>> getSystemSettingsByQuery(@Query("q") String query,
                                                          @Query("v") String representation);

    /**
     * Form create call.
     *
     * @param uuid the uuid
     * @param obj  the obj
     * @return the call
     */
    @POST("form/{uuid}/resource")
    Call<FormCreate> formCreate(@Path("uuid") String uuid,
                                @Body FormData obj);

    /**
     * Gets provider list.
     *
     * @return the provider list
     */
    @GET("provider?v=default")
    Call<Results<Provider>> getProviderList();

    /**
     * Delete provider call.
     *
     * @param uuid the uuid
     * @return the call
     */
    @DELETE("provider/{uuid}?!purge")
    Call<ResponseBody> deleteProvider(@Path("uuid") String uuid);

    /**
     * Add provider call.
     *
     * @param provider the provider
     * @return the call
     */
    @POST("provider")
    Call<Provider> addProvider(@Body Provider provider);

    /**
     * Add email call.
     *
     * @param emailRequest to Blopup
     * @return the call
     */
    @POST("email")
    Call<ResponseBody> sendEmail(@Body EmailRequest emailRequest);

    /**
     * Update provider call.
     *
     * @param uuid     the uuid
     * @param provider the provider
     * @return the call
     */
    @POST("provider/{uuid}")
    Call<Provider> UpdateProvider(@Path("uuid") String uuid,
                                  @Body Provider provider);

    /**
     * Gets allergies.
     *
     * @param uuid the uuid
     * @return the allergies
     */
    @GET("patient/{uuid}/allergy")
    Call<Results<Allergy>> getAllergies(@Path("uuid") String uuid);

    /**
     * Delete allergy call.
     *
     * @param patientUuid the patient uuid
     * @param allergyUuid the allergy uuid
     * @return the call
     */
    @DELETE("patient/{patientUuid}/allergy/{allergyUuid}")
    Call<ResponseBody> deleteAllergy(@Path("patientUuid") String patientUuid,
                                     @Path("allergyUuid") String allergyUuid);

    /**
     * Create allergy call.
     *
     * @param uuid          the uuid
     * @param allergyCreate the allergy create
     * @return the call
     */
    @POST("patient/{uuid}/allergy")
    Call<Allergy> createAllergy(@Path("uuid") String uuid,
                                @Body AllergyCreate allergyCreate);

    /**
     * Update allergy call.
     *
     * @param patientUuid   the patient uuid
     * @param allergyUuid   the allergy uuid
     * @param allergyCreate the allergy create
     * @return the call
     */
    @POST("patient/{patientUuid}/allergy/{allergyUuid}")
    Call<Allergy> updateAllergy(@Path("patientUuid") String patientUuid,
                                @Path("allergyUuid") String allergyUuid,
                                @Body AllergyCreate allergyCreate);
}
