package com.app.selfcare.services

import com.app.selfcare.data.*
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http.hasBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*


interface RequestInterface {

    @POST("create_anonymous_user/")
    fun sendDeviceId(@Body deviceId: DeviceId): Single<ResponseBody>

    @GET("questionarie")
    fun getQuestionnaire(
        @Query("therapy") therapy: String,
        @Query("device_id") deviceId: String
    ): Single<ResponseBody>

    @GET("questionnaire_list")
    fun getAllQuestionnaire(@Query("therapy") therapy: String): Single<ResponseBody>

    @POST("questionarie")
    fun sendAnswers(
        @Query("therapy") therapy: String,
        @Body answer: SendAnswer
    ): Single<ResponseBody>

    @POST("questionnaire_submit")
    fun sendAllAnswers(@Body answer: SendAnswer): Single<ResponseBody>

    @POST("verify_employee/")
    fun verifyEmp(@Body employee: Employee): Single<ResponseBody>

    @POST("patient/verify_user_details/")
    fun verifyUserDetail(@Body userDetails: UserDetails): Single<ResponseBody>

    @POST("register/")
    fun register(@Body register: Register): Single<ResponseBody>

    @POST("login/")
    fun login(@Body user: Login): Single<ResponseBody>

    @POST("send_otp/")
    fun sendOtp(@Body sendOtp: SendOtp): Single<ResponseBody>

    @POST("verify_otp/")
    fun verifyOtp(@Body verifyOtp: VerifyOtp): Single<ResponseBody>

    @GET("select_all/")
    fun getData(
        @Query("table_id") table_id: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getRequiredData(
        @Query("table_id") table_id: String,
        @Body id: PatientId,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getPersonalGoalData(
        @Query("table_id") table_id: String,
        @Body id: PersonalGoalData,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("payment_sheet/")
    fun getSelfPayDetails(
        @Body selfPay: SelfPay,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getPaymentStatus(
        @Query("table_id") table_id: String,
        @Body paymentStatus: PaymentStatus,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/available_doctor/")
    fun getTherapistList(
        @Body list: PatientId,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/time_slot")
    fun getTimeSlots(
        @Body timeSlots: TimeSlots,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun bookAppointment(
        @Query("table_id") table_id: String,
        @Body appt: AppointmentReq,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun changeAppointmentStatus(
        @Query("table_id") table_id: String,
        @Body appt: AppointmentReq,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun createJournalData(
        @Query("table_id") table_id: String,
        @Body journal: CreateJournal,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun createPersonalGoalData(
        @Query("table_id") table_id: String,
        @Body goal: CreatePersonalGoal,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @HTTP(method = "DELETE", path = "delete/", hasBody = true)
    fun deleteData(
        @Query("table_id") table_id: String, @Body dataId: DataId,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/my_appointment/")
    fun getAppointmentList(
        @Body list: AppointmentPatientId,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("patient/cancel_appointment/")
    fun cancelAppointment(
        @Body list: CancelAppointment,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendHealthInfo(
        @Query("table_id") table_id: String,
        @Body healthInfo: HealthInfo,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun updateProfileData(
        @Query("table_id") table_id: String,
        @Body profileData: PartProfileData,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/coaches_list/")
    fun getCoaches(
        @Body coachReqBody: CoachReqBody,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/verify_insurance/")
    fun insuranceVerifyApi(
        @Body insuranceVerifyReq: InsuranceVerifyReqBody,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendFeedback(
        @Query("table_id") table_id: String,
        @Body feedback: Feedback,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/assign_assessments_list/")
    fun getAssessments(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/assessments_data_list/")
    fun getAssessmentData(
        @Query("type") type: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/assessments_data_list/")
    fun sendAssessmentData(
        @Query("type") type: String,
        @Body answer: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/assign_assessments_list/")
    fun viewAssessment(
        @Query("assessment_id") assessment_id: Int,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/recommended_data_list/")
    fun getRecommendedData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/group_appointment_list/")
    fun getGroupAppointmentList(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/consent_list/")
    fun getConsentsList(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("get_agora_token/")
    fun getToken(
        @Body list: GetToken,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>
}