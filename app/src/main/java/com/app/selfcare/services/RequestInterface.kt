package com.app.selfcare.services

import com.app.selfcare.data.*
import com.google.gson.JsonElement
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.Response
import okhttp3.ResponseBody
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

    @POST("questionarie")
    fun sendAnswers(
        @Query("therapy") therapy: String,
        @Body answer: SendAnswer
    ): Single<ResponseBody>

    @POST("verify_employee/")
    fun verifyEmp(@Body employee: Employee): Single<ResponseBody>

    @POST("register/")
    fun register(@Body register: Register): Single<ResponseBody>

    @POST("login/")
    fun login(@Body user: Login): Single<ResponseBody>

    @POST("send_otp/")
    fun sendOtp(@Body sendOtp: SendOtp): Single<ResponseBody>

    @POST("verify_otp/")
    fun verifyOtp(@Body verifyOtp: VerifyOtp): Single<ResponseBody>

    @GET("select_all/")
    fun getPlanList(@Query("table_id") table_id: String): Single<ResponseBody>

    @POST("payment_sheet/")
    fun getSelfPayDetails(@Body selfPay: SelfPay): Single<ResponseBody>

    @POST("select/")
    fun getPaymentStatus(@Query("table_id") table_id: String,@Body paymentStatus: PaymentStatus): Single<ResponseBody>

    @POST("patient/available_doctor/")
    fun getTherapistList(@Body list: PatientId): Single<ResponseBody>

    @POST("patient/time_slot")
    fun getTimeSlots(@Body timeSlots: TimeSlots): Single<ResponseBody>

    @PUT("update/")
    fun bookAppointment(@Query("table_id") table_id: String,@Body appt: AppointmentReq): Single<ResponseBody>

}