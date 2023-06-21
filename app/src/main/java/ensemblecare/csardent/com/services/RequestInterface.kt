package ensemblecare.csardent.com.services

import ensemblecare.csardent.com.data.*
import io.reactivex.Single
import okhttp3.ResponseBody
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
    fun getAllQuestionnaire(@Query("new") therapy: String): Single<ResponseBody>

    @POST("questionarie")
    fun sendAnswers(
        @Query("therapy") therapy: String,
        @Body answer: SendAnswer
    ): Single<ResponseBody>

    @POST("questionnaire_submit")
    fun sendAllAnswers(
        @Query("new") new: String,
        @Body answer: SendAnswer
    ): Single<ResponseBody>

    @POST("verify_employee/")
    fun verifyEmp(@Body employee: Employee): Single<ResponseBody>

    @POST("patient/verify_user_details/")
    fun verifyUserDetail(@Body userDetails: UserDetails): Single<ResponseBody>

    @POST("register/")
    fun register(@Body register: Register): Single<ResponseBody>

    @POST("login/")
    fun login(@Body user: Login): Single<ResponseBody>

    @POST("carebuddy/login/")
    fun careBuddyLogin(@Body user: CareBuddyLogin): Single<ResponseBody>

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
        @Body id: Patient,
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
        @Query("type") type: String,
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
        @Body list: Patient,
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
        @Query("table_id") table_id: String,
        @Body dataId: DataId,
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

    @POST("create/")
    fun sendGroupVideoFeedback(
        @Query("table_id") table_id: String,
        @Body feedback: GroupVideoCallFeedback,
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

    @POST("get_agora_token/")
    fun getGroupApptToken(
        @Body list: GetGroupApptToken,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("get_agora_token/")
    fun getTrainingSessionToken(
        @Body list: GetTrainingSessionToken,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/patient_availability/")
    fun clientAvailability(
        @Body clientAvailability: ClientAvailability,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/wellness_dashboard/")
    fun getWellnessDashboardData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/exercise_data/")
    fun getExerciseDashboardData(
        @Query("for") name: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/nutrition_data/")
    fun getNutritionDashboardData(
        @Query("for") name: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/mindfulness_data/")
    fun getMindfulnessDashboardData(
        @Query("for") name: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/yoga_data/")
    fun getYogaDashboardData(
        @Query("for") name: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/{type}")
    fun getDetailData(
        @Path("type") type: String,
        @Query("category") category: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("password_reset/")
    fun sendEmail(
        @Body sendEmail: SendEmail,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("password_reset/confirm/")
    fun confirmPassword(
        @Body confirmPassword: ConfirmPassword,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/{type}/")
    fun sendFavoriteData(
        @Path("type") type: String,
        @Body favoriteData: FavoriteData,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/{type}/")
    fun getFavoriteData(
        @Path("type") type: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun updateJournal(
        @Query("table_id") table_id: String,
        @Body updateJournal: UpdateJournal,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/resource_data/")
    fun getResourceDashboardData(
        @Query("for") name: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/resource_data/")
    fun sendResourceFavoriteData(
        @Body favoriteData: FavoriteData,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/resource_data/")
    fun getResourceFavoriteData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/documents_data/")
    fun getDocumentsData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/plan_details/")
    fun getPlanSettingsData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getNotificationSettingsData(
        @Query("table_id") table_id: String,
        @Body id: ClientId,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun updateNotificationSettingsData(
        @Query("table_id") table_id: String,
        @Body updateNotificationSettings: UpdateNotificationSettings,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("care_plan_list")
    fun getCarePlanData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("care_plan")
    fun getDayWiseCarePlanData(
        @Query("day") day: Int,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getToDoData(
        @Query("table_id") table_id: String,
        @Body id: PatientId,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun createToDoData(
        @Query("table_id") table_id: String,
        @Body createToDo: CreateToDo,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun updateToDoData(
        @Query("table_id") table_id: String,
        @Body updateToDo: UpdateToDo,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/notification_api")
    fun getDashboardNotification(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("covered_type/")
    fun coveredType(
        @Body employee: Employee,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/consent_list/")
    fun sendFormSignature(
        @Body formSignature: FormSignature,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun sendApptStatus(
        @Query("table_id") table_id: String,
        @Body apptStatus: AppointmentStatus,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendExerciseCarePlanPendingTask(
        @Query("table_id") table_id: String,
        @Body exerciseCarePlan: ExerciseCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendNutritionCarePlanPendingTask(
        @Query("table_id") table_id: String,
        @Body nutritionCarePlan: NutritionCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendYogaCarePlanPendingTask(
        @Query("table_id") table_id: String,
        @Body yogaCarePlan: YogaCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendMindfulnessCarePlanPendingTask(
        @Query("table_id") table_id: String,
        @Body mindfulnessCarePlan: MindfulnessCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @PUT("update/")
    fun updateNotificationStatus(
        @Query("table_id") table_id: String,
        @Body notifyStatus: NotifyStatus,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @HTTP(method = "DELETE", path = "delete/", hasBody = true)
    fun sendYogaCarePlanDeleteTask(
        @Query("table_id") table_id: String,
        @Body removeCarePlan: RemoveCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @HTTP(method = "DELETE", path = "delete/", hasBody = true)
    fun sendNutritionCarePlanDeleteTask(
        @Query("table_id") table_id: String,
        @Body removeCarePlan: RemoveCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @HTTP(method = "DELETE", path = "delete/", hasBody = true)
    fun sendExerciseCarePlanDeleteTask(
        @Query("table_id") table_id: String,
        @Body removeCarePlan: RemoveCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @HTTP(method = "DELETE", path = "delete/", hasBody = true)
    fun sendMindfulnessCarePlanDeleteTask(
        @Query("table_id") table_id: String,
        @Body removeCarePlan: RemoveCarePlan,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/music_data/")
    fun getMusicDashboardData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("select_all")
    fun getEventData(
        @Query("table_id") table_id: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getEventDetail(
        @Query("table_id") table_id: String,
        @Body fetchCareBuddyDetail: FetchCareBuddyDetail,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getCareBuddyData(
        @Query("table_id") table_id: String,
        @Body fetchCareBuddyList: FetchCareBuddyList,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun fetchCareBuddyDetail(
        @Query("table_id") table_id: String,
        @Body fetchCareBuddyDetail: FetchCareBuddyDetail,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("carebuddy/create")
    fun createCareBuddyData(
        @Query("table_id") table_id: String,
        @Body addCareBuddy: AddCareBuddy,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendFile(
        @Query("table_id") table_id: String,
        @Body fileDetails: FileDetails,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendFileGroupVideo(
        @Query("table_id") table_id: String,
        @Body fileDetails: GroupVideoFileDetails,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @HTTP(method = "DELETE", path = "delete/", hasBody = true)
    fun deleteToDo(
        @Query("table_id") table_id: String,
        @Body deleteToDo: DeleteToDo,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("carebuddy/client_list")
    fun getCareBuddyDashboardData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("carebuddy/care_plan_list/{id}")
    fun getCareBuddyCarePlanDayWiseData(
        @Path("id") id: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("carebuddy/care_plan/{id}")
    fun getCareBuddyDayWiseCarePlanData(
        @Path("id") id: String,
        @Query("day_no") dayNo: Int,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("carebuddy/add_carebuddy")
    fun searchByEmail(
        @Query("email") email: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("carebuddy/add_carebuddy")
    fun sendSelectedEmail(
        @Query("email", encoded = true) email: String,
        @Body sendSelectedEmail: SendSelectedEmail,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("patient/interests/")
    fun getInterestData(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/interests/")
    fun sendInterestData(
        @Body sendSelectedInterests: SendSelectedInterests,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("{partLink}")
    fun sendAssignedToDoSign(
        @Path("partLink") partLink: String,
        @Body toDoSignature: ToDoSignature
    ): Single<ResponseBody>

    @POST("create/")
    fun sendCallLog(
        @Query("table_id") table_id: String,
        @Body callLog: CallLog,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("patient/notification_api/")
    fun setCareBuddyNotification(
        @Body type: NotificationType,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("select_all/")
    fun getForumData(
        @Query("table_id") table_id: String,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("get_agora_token/")
    fun getChatRoomToken(
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("create/")
    fun sendChatRoomMsg(
        @Query("table_id") table_id: String,
        @Body chatMsg: ChatMsg,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getPreviousMsgs(
        @Query("table_id") table_id: String,
        @Body prevMsg: PrevMsg,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @POST("select/")
    fun getNextPreviousMsgs(
        @Query("page") page: String,
        @Query("table_id") table_id: String,
        @Body prevMsg: PrevMsg,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>

    @GET("user_basic_details")
    fun getUserBasicDetails(
        @Query("user_id") user_id: Int,
        @Header("Authorization") auth: String
    ): Single<ResponseBody>
}