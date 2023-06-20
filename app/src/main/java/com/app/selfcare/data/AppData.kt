package com.app.selfcare.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Question(
    val question_id: Int,
    val question: String,
    val option_id: String,
    val next: String,
    val is_multiple: Boolean,
    var no_of_options: String,
    val options: ArrayList<Answers>
)

data class Answers(
    val id: Int,
    val data: String,
    val rating: Int,
    val created_at: String,
    val option_id: String
)

data class OptionModel(
    val text: String,
    var isSelected: Boolean
)

@Parcelize
data class Plan(
    val id: Int,
    val name: String,
    val stripe_product_id: String,
    val is_addon: Boolean,
    val monthly_price: String,
    val quarterly_price: String,
    val annually_price: String,
    val no_of_sessions: Int
) : Parcelable

@Parcelize
data class PlanTherapy(
    val product_id: Int,
    val name: String,
    val stripe_product_id: String,
    val has_addon: Boolean,
    val monthly_price: String,
    val quaterly_price: String,
    val annually_price: String,
    val add_on_plan: AddOn
) : Parcelable

@Parcelize
data class AddOn(
    val id: Int,
    val name: String,
    val stripe_product_id: String,
    val is_addon: Boolean,
    val monthly_price: String,
    val quarterly_price: String?,
    val annually_price: String,
    val no_of_sessions: Int
) : Parcelable

@Parcelize
data class Therapist(
    val doctor_id: String,
    val doctor_public_id: String,
    val ssn: String,
    val first_name: String,
    val middle_name: String,
    val last_name: String,
    val doctor_type: String,
    val gender: String,
    val dob: String,
    val photo: String,
    val qualification: String,
    val description: String,
    val years_of_experience: String,
    val street: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val country: String,
    val emergency_phone: String,
    val practice_state: String,
    val license_number: String,
    val ratings: String,
    val preffered_language: String,
    val user: Int,
    val appointment: AvailableAppointment
) : Parcelable

@Parcelize
data class AvailableAppointment(
    val appointment_id: Int,
    val appointment_uid: String,
    val status: Int,
    val date: String,
    val is_book: Boolean,
    val actual_start_time: String,
    val actual_end_time: String,
    val duration: String,
    val cancelled_by: String,
    val type_of_visit: String,
    val booking_date: String,
    val prescription_1: String,
    val prescription_2: String,
    val prescription_3: String,
    val doctor: Int,
    val patient: Int,
    val time_slot: AvailableTimeSlot,
    val on_sameday: Boolean
) : Parcelable

@Parcelize
data class CancelledAppt(
    val appointment_id: Int,
    val appointment_uid: String,
    val status: Int,
    val date: String,
    val is_book: Boolean,
    val actual_start_time: String,
    val actual_end_time: String,
    val duration: String,
    val cancelled_by: String,
    val type_of_visit: String,
    val booking_date: String,
    val prescription_1: String,
    val prescription_2: String,
    val prescription_3: String,
    val doctor: DoctorDetails,
    val patient: Int,
    val time_slot: AvailableTimeSlot,
    val on_sameday: Boolean
) : Parcelable

@Parcelize
data class AvailableTimeSlot(
    val duration: String,
    val starting_time: String,
    val ending_time: String,
    val time_slot_id: String
) : Parcelable

@Parcelize
data class DoctorDetails(
    val id: Int,
    val name: String,
    val designation: String,
    val photo: String
) : Parcelable

data class Login(
    val email: String,
    val password: String,
    val registration_id: String
)

data class CareBuddyLogin(
    val email: String,
    val password: String
)

data class DeviceId(
    val device_id: String,
    val therapy: Int
)

data class SendAnswer(
    val anonymous_user_id: Int,
    val data: ArrayList<EachAnswer>
)

data class EachAnswer(
    val question_id: Int,
    val answer: Answer
)

data class Answer(
    val option: ArrayList<Int>,
    val other_text: String = ""
)

data class Employee(
    val covered_type: String,
    val employee_id: String,
    val employer: String,
    val access_code: String
)

data class SendEmail(
    val email: String
)

data class ConfirmPassword(
    val password: String,
    val token: String
)

data class FavoriteData(
    val id: Int,
    val type: String,
    val is_favourite: Boolean
)

data class UserDetails(
    val email: String,
    val phone: String
)

@Parcelize
data class Register(
    val email: String,
    val phone_0: String,
    val password1: String,
    val password2: String,
    val first_name: String,
    val last_name: String,
    val middle_name: String,
    val dob: String,
    val device_id: String,
    val is_employee: Boolean,
    val employer: String,
    val employee_id: String,
    val gender: String,
    val preffered_language: String,
    val access_code: String = "XYZ123",
    val ethnicity: String,
    val role: String
) : Parcelable

data class SendOtp(
    val user_email: String,
    val user_phone_no: String
)

data class VerifyOtp(
    val sid: String,
    val otp: String
)

data class SelfPay(
    val patient_id: String,
    val add_on: String,
    val plan_id: Int
)

data class PaymentStatus(
    val id: String
)

data class PatientId(
    val patient_id: Int,
    val is_assign: String
)

data class Patient(
    val patient_id: Int
)

data class ClientId(
    val client: Int
)

data class PersonalGoalData(
    val patient_id: Int,
    val goal_type: String = "Personal"
)

data class TimeSlots(
    val doctor_public_id: String,
    val date: String
)

data class CreateJournal(
    val name: String,
    val description: String,
    val patient_id: String,
    val journal_date: String,
    val journal_time: String
)

data class UpdateJournal(
    val id: Int,
    val name: String,
    val description: String
)

data class CreatePersonalGoal(
    val title: String,
    val description: String,
    val goal_type: String,
    val start_date: String,
    val duration: Int,
    val frequency: String,
    val patient_id: String
)

data class DataId(
    val id: Int
)

data class AppointmentPatientId(
    val patient: Int
)

data class CoachReqBody(
    val type: String,
    val specialization: ArrayList<String>
)

data class CancelAppointment(
    val patient_id: Int,
    val appointment_id: Int
)

data class GetToken(
    val appointment_id: Int
)

data class GetGroupApptToken(
    val group_appointment_id: Int,
    val email: String
)

data class GetTrainingSessionToken(
    val training_appointment_id: Int,
    val email: String
)

@Parcelize
data class ClientAvailability(
    val patient: Int,
    val days: ArrayList<String>,
    val time: ArrayList<String>
) : Parcelable

data class HealthInfo(
    val patient: Int,
    val total_steps: Int,
    val total_distance: Int,
    val total_calaroies: Int,
    val heart_beat: Int
)

data class ProfileData(
    val patient_id: Int,
    val ssn: String,
    val first_name: String,
    val middle_name: String,
    val last_name: String,
    val gender: String,
    val dob: String,
    val is_employee: String,
    val employee_id: String,
    val employer: String,
    val device_id: String,
    val stripe_id: String,
    val street: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val relation_to: String,
    val country: String,
    val emergency_phone: String,
    val marital_status: String,
    val race: String,
    val ethnicity: String,
    val state_of_living: String,
    val referral_source: String,
    val is_consent_verified: String,
    val consent_verified_datetime: String,
    val consent_photo: String,
    val level_of_care: String,
    val chosen_therapy: String,
    val selected_plan: String,
    val patient_severity_score: String,
    val payment_type: String,
    val selected_service: String,
    val selected_dissorder: String,
    val photo: String,
    val address: String,
    val address1: String,
    val created_at: String,
    val updated_at: String,
    val note: String,
    val preffered_language: String,
    val role: String,
    val user: String,
    val parental_consent: String
)

data class PartProfileData(
    val patient_id: Int,
    val first_name: String,
    val middle_name: String,
    val last_name: String,
    val dob: String,
    val photo: String?,
    val emergency_phone: String,
    val city: String,
    val state: String,
    val address: String,
    val marital_status: String?,
    val zipcode: String,
    val relation_to: String?,
    val address1: String,
    val country: String,
    val gender: String?,
    val preffered_language: String?,
    val ethnicity: String?,
    val role: String?
)

data class AppointmentReq(
    val appointment_id: String,
    val patient: String,
    val is_book: Boolean,
    val booking_date: String,
    val type_of_visit: String,
    val status: Int,
    val consent_sign_image: String,
    val prescription_1: String,
    val prescription_2: String,
    val prescription_3: String
)

data class AppointmentStatus(
    val appointment_id: String,
    val actual_start_time: String,
    val actual_end_time: String,
    val duration: String,
    val status: Int
)

@Parcelize
data class TransactionStatus(
    val id: String,
    val transaction_id: String,
    val customer_email: String,
    val amount: Int,
    val stripe_payment_intent: String,
    val has_paid: Boolean,
    val product: Int
) : Parcelable

@Parcelize
data class Articles(
    val id: Int,
    val name: String,
    val description: String,
    val banner_image: String,
    val article_url: String,
    val published_date: String,
    val is_favourite: Boolean
) : Parcelable

@Parcelize
data class Podcast(
    val id: Int,
    val name: String,
    val description: String,
    val podcast_image: String,
    val artist: String,
    val podcast_url: String,
    val is_favourite: Boolean
) : Parcelable

data class JournalDashboard(
    val count: Int,
    val next: String,
    val previous: String,
    val results: ArrayList<Journal>
)

@Parcelize
data class Journal(
    val id: Int,
    val name: String,
    val description: String,
    val journal_date: String,
    val patient_id: Int,
    val journal_time: String,
    val created_on: String
) : Parcelable

@Parcelize
data class Video(
    val id: Int,
    val name: String,
    val description: String,
    val video_url: String,
    val banner: String,
    val date: String,
    val time: String,
    val video_file: String,
    val for_wellness: String,
    val therapy: String,
    val is_favourite: Boolean
) : Parcelable

@Parcelize
data class RecommendedData(
    var type: Int = -1,
    var id: Int = -1,
    var name: String = "",
    var description: String = "",
    var video_url: String = "",
    var podcast_image: String = "",
    var artist: String = "",
    var podcast_url: String = "",
    var banner_image: String = "",
    var article_url: String = "",
    var published_date: String = ""
) : Parcelable

@Parcelize
data class Recommended(
    val videos: ArrayList<Video> = ArrayList(),
    val podcasts: ArrayList<Podcast> = ArrayList(),
    val articles: ArrayList<Articles> = ArrayList(),
    val provider_goals: ArrayList<Goal> = ArrayList(),
) : Parcelable

@Parcelize
data class Goal(
    val id: Int,
    val title: String,
    val description: String,
    val start_date: String,
    val duration: Int
) : Parcelable

@Parcelize
data class GetAppointmentList(
    val today: ArrayList<GetAppointment>,
    val upcoming: ArrayList<GetAppointment>,
    val past: ArrayList<GetAppointment>
) : Parcelable

@Parcelize
data class GetAppointment(
    val group_name: String,
    val doctor_first_name: String,
    val doctor_last_name: String,
    val doctor_designation: String,
    val doctor_photo: String,
    val group_appointment: GroupAppointment?,
    val appointment: Appointment?,
    val meeting_title: String,
    val channel_name: String,
    val rtc_token: String,
    val rtm_token: String,
    val rtc_token_doctor: String,
    val duration: Int,
    val description: String,
    val meeting_date: String,
    val is_group_appointment: Boolean,
    val id: Int,
    val appointment_type: String,
    val uid: String,
    val title: String,
    val date: String,
    val start_time: String,
    val end_time: String,
    val is_complete: Boolean,
    val host: SessionHost?
) : Parcelable

@Parcelize
data class SessionHost(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val photo: String,
    val user: String
) : Parcelable

@Parcelize
data class Appointment(
    val appointment_id: Int,
    val appointment_uid: String,
    val status: Int,
    val date: String,
    val is_book: String,
    val type_of_visit: String,
    val booking_date: String,
    val consent_sign_image: String,
    val prescription_1: String,
    val prescription_2: String,
    val prescription_3: String,
    val time_slot: AppointmentTimeSlot
) : Parcelable

@Parcelize
data class AppointmentTimeSlot(
    val time_slot_id: String,
    val starting_time: String,
    val ending_time: String,
    val duration: String,
    val created_at: String,
    val updated_at: String
) : Parcelable

@Parcelize
data class TimeSlot(
    val appointment_id: String,
    val doctor_id: String,
    val date: String,
    val time_slot_start: String,
    val time_slot_end: String,
    val time_slot_id: String,
    var isSelected: Boolean
) : Parcelable

data class Coaches(
    val coach_detail_id: Int,
    val type: String,
    val coach: CoachDetail,
    val preference1: Preference,
    val preference2: Preference,
    val preference3: Preference,
    val preference4: Preference,
    val preference5: Preference,
    val preference6: Preference,
    val specialization: ArrayList<Preference>,
)

data class CoachDetail(
    val doctor_id: Int,
    val doctor_public_id: String,
    val ssn: String,
    val first_name: String,
    val middle_name: String,
    val last_name: String,
    val doctor_type: String,
    val dob: String,
    val qualification: String,
    val years_of_experiance: String,
    val street: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val county: String,
    val emergency_phone: String,
    val gender: String,
    val practice_state: String,
    val license_number: String,
    val user: String,
    val therapy: ArrayList<Int>,
    val dissorder: ArrayList<Int>,
    val available_on_day: ArrayList<Int>,
    val available_on_time: ArrayList<Int>,
)

data class Preference(
    val id: Int,
    val name: String
)

data class InsuranceVerifyReqBody(
    val patient: Int,
    val insurance_company: String,
    val plan_id: String,
    val member_id: String,
    val group_id: String,
    val member_name: String,
    val scheme: String,
    val photo: String,
    val photo_2: String
)

data class Feedback(
    val patient: Int,
    val appointment: Int,
    val therapist_rating: Double,
    val therapist_review: String,
    val service_rating: Double,
    val service_review: String
)

data class GroupVideoCallFeedback(
    val patient: Int,
    val group_appointment: Int,
    val therapist_rating: Double,
    val therapist_review: String,
    val service_rating: Double,
    val service_review: String
)

@Parcelize
data class Assessments(
    val id: Int,
    val assign_date: String,
    val filled_by: String,
    val is_completed: Boolean,
    val patient: String,
    val doctor_first_name: String,
    val doctor_last_name: String,
    val type_of_assessment: String
) : Parcelable

@Parcelize
data class ViewAssessment(
    val questions_answers: ArrayList<QuestionAnswer>,
    val Total_score: String,
    val patient_score: String,
    val id: String
) : Parcelable

@Parcelize
data class QuestionAnswer(
    val question: String,
    val answer: String
) : Parcelable

@Parcelize
data class AssessmentQuestion(
    val id: Int,
    val type_of_assessment: String,
    val question: String,
    val question_no: Int
) : Parcelable

@Parcelize
data class AssessmentAnswer(
    val id: Int,
    val type_of_assessment: String,
    val answer: String,
    val value: Int
) : Parcelable

@Parcelize
data class GroupAppointment(
    val id: Int,
    val group_appointment_uid: String,
    val date: String,
    val duration: String,
    val time: String,
    val select_am_or_pm: String,
    val Appointment_description: String,
    val Appointment_note: String,
    val status: String,
    val group: Int,
    val doctor: Int,
    val group_patient: ArrayList<Int>
) : Parcelable

@Parcelize
data class ConsentRois(
    val key: String,
    val text: String,
    val isCompleted: Boolean,
    val patient: String,
    val doctor: String,
    val patient_status: String,
    val doctor_status: String
) : Parcelable

data class CoachType(
    val mainText: String,
    val secondaryText: String,
    val subText: String,
    val image: Int
)

@Parcelize
data class Nutrition(
    val name: String,
    val image: Int
) : Parcelable

@Parcelize
data class AvailabilityData(
    val name: String,
    var isSelected: Boolean = false
) : Parcelable

@Parcelize
data class ExerciseDashboard(
    val id: Int,
    val exercise_name: String,
    val category: String,
    val video: String,
    val image: String,
    val type: String,
    val url: String,
    val likes_count: String,
    val related_videos: Video,
    val related_articles: Articles,
    val related_podcast: Podcast
) : Parcelable

@Parcelize
data class NutritionDashboard(
    val id: Int,
    val nutrition_name: String,
    val category: String,
    val video: String,
    val image: String,
    val type: String,
    val url: String,
    val likes_count: String,
    val calories: String,
    val time_taken: String,
    val related_videos: Video,
    val related_articles: Articles,
    val related_podcast: Podcast
) : Parcelable

@Parcelize
data class MindfulnessDashboard(
    val id: Int,
    val mindfulness_name: String,
    val category: String,
    val video: String,
    val image: String,
    val type: String,
    val url: String,
    val likes_count: String,
    val related_videos: Video,
    val related_articles: Articles,
    val related_podcast: Podcast
) : Parcelable

@Parcelize
data class YogaDashboard(
    val id: Int,
    val yoga_name: String,
    val category: String,
    val video: String,
    val image: String,
    val type: String,
    val url: String,
    val likes_count: String,
    val related_videos: Video,
    val related_articles: Articles,
    val related_podcast: Podcast
) : Parcelable

@Parcelize
data class Documents(
    val date: String,
    val Appointment: ArrayList<AppointmentDocumentData>?,
    val Consents: ArrayList<ConsentsRoisDocumentData>?,
    val Forms: ArrayList<ConsentsRoisDocumentData>?
) : Parcelable

@Parcelize
data class AppointmentDocumentData(
    val id: Int,
    val date: String,
    val time: String,
    val section: String,
    val title: String,
    val prescriptions: ArrayList<String>,
    val insurance: ArrayList<String>,
    val consents: ArrayList<String>
) : Parcelable

@Parcelize
data class ConsentsRoisDocumentData(
    val pk: String,
    val name: String,
    val pdf_url: String,
    val date: String,
    val time: String,
    val category: String,
    val description: String,
    val title: String,
    val section: String
) : Parcelable

data class ToDoDashboard(
    val count: Int,
    val next: String,
    val previous: String,
    val results: ArrayList<ToDoData>
)

@Parcelize
data class ToDoData(
    val id: Int,
    val assessment_link: String,
    val title: String,
    val description: String,
    val end_date: String,
    val created_on: String,
    val is_completed: Boolean,
    val is_assign: String,
    val updated_on: String,
    val patient_id: Int,
    val assessment: Int
) : Parcelable

@Parcelize
data class NotificationData(
    val id: Int,
    val date: String,
    val time: String,
    val notification_desc: String,
    val is_group_appointment: Boolean
) : Parcelable

@Parcelize
data class PlanSettingsData(
    val current_subscription: CurrentSubscription,
    val has_addon: Boolean,
    val add_on: PlanDetailAddOn,
    val payment_type: String
) : Parcelable

@Parcelize
data class PlanDetailAddOn(
    val plan_detail: String,
    val price: String
) : Parcelable

@Parcelize
data class CurrentSubscription(
    val id: Int,
    val plan_detail: String,
    val price: Int,
    val expired_at: String,
    val created_at: String,
    val updated_on: String,
    val plan: Int,
    val user: Int,
    val transaction: String
) : Parcelable

@Parcelize
data class CarePlans(
    val title: String,
    val current_day: Int,
    val days: ArrayList<CareDay>
) : Parcelable

@Parcelize
data class CareDay(
    val day: Int,
    val total_task: Int,
    val completed: Int,
    val progress: Double
) : Parcelable

@Parcelize
data class DayWiseCarePlan(
    val id: Int,
    val patient: Int,
    val coach: CarePlanCoach,
    val aim: String,
    val level: String,
    val notes: String,
    val total_days: Int,
    val occupation: String,
    val interests: String,
    val plan: CareDayPlan
) : Parcelable

@Parcelize
data class CarePlanCoach(
    val name: String?,
    val type: String
) : Parcelable

@Parcelize
data class CareDayPlan(
    val day: Int,
    val task_completed: CareDayTaskCompletion,
    val calories: CareDayTaskCompletion,
    val exercise: CareDayTaskCompletion,
    val yoga: CareDayTaskCompletion,
    val mindfulness: CareDayTaskCompletion,
    val nutrition: CareDayTaskCompletion,
    val music: CareDayTaskCompletion,
    val tasks: CareDayTasks
) : Parcelable

@Parcelize
data class CareDayTaskCompletion(
    val total: String,
    val completed: String,
    val total_time: String,
    val completed_time: String
) : Parcelable

@Parcelize
data class CareDayTasks(
    val yoga: ArrayList<CareDayIndividualTaskDetail>,
    val nutrition: ArrayList<CareDayIndividualTaskDetail>,
    val exercise: ArrayList<CareDayIndividualTaskDetail>,
    val mindfulness: ArrayList<CareDayIndividualTaskDetail>,
    val music: ArrayList<CareDayIndividualTaskDetail>
) : Parcelable

@Parcelize
data class CareDayIndividualTaskDetail(
    val id: Int,
    val day_no: Int,
    val time: String,
    val duration: String,
    val date: String?,
    val is_completed: Boolean,
    val task_input_id: Int,
    val plan: Int,
    val task_detail: CareDayTaskDetail
) : Parcelable

@Parcelize
data class CareDayTaskDetail(
    val id: Int,
    val yoga: Int = 0,
    val nutrition: Int = 0,
    val exercise: Int = 0,
    val mindfulness: Int = 0,
    val music: Int = 0,
    val aim: String,
    val title: String,
    val time_taken: String,
    val reps: String,
    val count: Int,
    val details: TaskDetails
) : Parcelable

@Parcelize
data class TaskDetails(
    val yoga_name: String,
    val nutrition_name: String,
    val exercise_name: String,
    val mindfulness_name: String,
    val music_name: String,
    val category: String,
    val video: String,
    val image: String,
    val type: String,
    val url: String
) : Parcelable

@Parcelize
data class NotificationSettings(
    val id: Int,
    val appointment_created: Boolean,
    val appointment_started: Boolean,
    val appointment_cancelled: Boolean,
    val appointment_completed: Boolean,
    val consent: Boolean,
    val provider: Boolean,
    val task_assigned: Boolean,
    val task_completed: Boolean,
    val task_missed: Boolean,
    val email_notification: Boolean,
    val wellness: Boolean,
    val updated_on: String,
    val client: Int,
) : Parcelable

data class UpdateNotificationSettings(
    val id: Int,
    val client: Int,
    val appointment_created: Boolean,
    val appointment_started: Boolean,
    val appointment_cancelled: Boolean,
    val appointment_completed: Boolean,
    val consent: Boolean,
    val provider: Boolean,
    val task_assigned: Boolean,
    val task_completed: Boolean,
    val task_missed: Boolean,
    val email_notification: Boolean,
    val wellness: Boolean
)

@Parcelize
data class CreateToDo(
    val patient_id: Int,
    val title: String,
    val description: String,
    val end_date: String,
    val is_completed: Boolean
) : Parcelable

@Parcelize
data class UpdateToDo(
    val id: Int,
    val patient_id: Int,
    val title: String,
    val description: String,
    val end_date: String,
    val is_completed: Boolean
) : Parcelable

@Parcelize
data class CancelledAppointmentNotify(
    val id: Int,
    val title: String,
    val type: String,
    val description: String,
    val extra_data: PrevNextAppt
) : Parcelable

@Parcelize
data class PrevNextAppt(
    val prev_appt_details: CancelledAppt,
    val next_appt_detials: CancelledAppt
) : Parcelable

@Parcelize
data class SubscriptionStatus(
    val id: Int,
    val title: String,
    val type: String,
    val description: String,
    val extra_data: SubscriptionDetail
) : Parcelable

@Parcelize
data class SubscriptionDetail(
    val pending_days: Int,
    val is_active: Boolean
) : Parcelable

@Parcelize
data class DailyInspiration(
    val id: Int,
    val title: String,
    val type: String,
    val description: String,
    val extra_data: DailyInspirationDetail
) : Parcelable

@Parcelize
data class DailyInspirationDetail(
    val text: String,
    val author: String,
    val image: String
) : Parcelable

@Parcelize
data class ConsentRoisFormsNotify(
    val id: Int,
    val title: String,
    val type: String,
    val description: String,
    val extra_data: ConsentRoisPk
) : Parcelable

@Parcelize
data class ConsentRoisPk(
    val pk: Int,
    val category: String?
) : Parcelable

data class FormSignature(
    val name: String,
    val pk: Int,
    val patient_signature: String
)

@Parcelize
data class ExerciseCarePlan(
    val patient: Int,
    val date: String,
    val care_plan: Int,
    val is_completed: Boolean,
    val exercise_plan: Int,
    val exercise_task: Int,
    val time: String
) : Parcelable

@Parcelize
data class NutritionCarePlan(
    val patient: Int,
    val date: String,
    val care_plan: Int,
    val is_completed: Boolean,
    val nutrition_plan: Int,
    val nutrition_task: Int,
    val time: String
) : Parcelable

@Parcelize
data class YogaCarePlan(
    val patient: Int,
    val date: String,
    val care_plan: Int,
    val is_completed: Boolean,
    val yoga_plan: Int,
    val yoga_task: Int,
    val time: String
) : Parcelable

@Parcelize
data class RemoveCarePlan(
    val id: Int
) : Parcelable

@Parcelize
data class DeleteToDo(
    val id: Int
) : Parcelable

@Parcelize
data class MindfulnessCarePlan(
    val patient: Int,
    val date: String,
    val care_plan: Int,
    val is_completed: Boolean,
    val mindfulness_plan: Int,
    val mindfulness_task: Int,
    val time: String
) : Parcelable

@Parcelize
data class NotifyStatus(
    val id: Int,
    val is_read: String = "yes"
) : Parcelable

@Parcelize
data class PlanDetails(
    val id: Int,
    val plan_detail: String,
    val price: String,
    val expired_at: String,
    val created_at: String,
    val updated_on: String,
    val plan: Int,
    val user: Int,
    val transaction: String?,
    val vob_verified: String
) : Parcelable

@Parcelize
data class InsuranceVerifiedDetails(
    val patient_insurance_id: Int,
    val insurance_company: String,
    val plan_id: String,
    val scheme: String,
    val member_id: String,
    val group_id: String,
    val member_name: String,
    val photo: String,
    val vob_verified: String,
    val patient: Int
) : Parcelable

@Parcelize
data class EventCommunity(
    val id: Int,
    val title: String,
    val date: String,
    val start_time: String,
    val end_time: String,
    val address: String,
    val description: String,
    val image: String
) : Parcelable

data class FetchCareBuddyList(
    val client: Int
)

data class FetchCareBuddyDetail(
    val id: Int
)

@Parcelize
data class CareBuddy(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val gender: String,
    val email: String,
    val phone: String,
    val relation: String,
    val state: String,
    val city: String,
    val zip_code: String,
    val country: String,
    val address: String,
    val address1: String,
    val photo: String,
    val is_verified: Boolean,
    val client: Int
) : Parcelable

@Parcelize
data class AddCareBuddy(
    val client: Int,
    val first_name: String,
    val last_name: String,
    val gender: String,
    val email: String,
    val phone: String,
    val relation: String,
    val state: String,
    val city: String,
    val zip_code: String,
    val country: String,
    val address: String,
    val address1: String
) : Parcelable

@Parcelize
data class FileDetails(
    val user: Int,
    val appt: String,
    val file_name: String,
    val file_ext: String,
    val file: String
) : Parcelable

@Parcelize
data class GroupVideoFileDetails(
    val user: Int,
    val group_appt: String,
    val file_name: String,
    val file_ext: String,
    val file: String
) : Parcelable

@Parcelize
data class CareBuddyDashboard(
    val name: String,
    val phone: String,
    val pk: String
) : Parcelable

@Parcelize
data class InterestData(
    val interests: ArrayList<String>
) : Parcelable

@Parcelize
data class SendSelectedEmail(
    val email: String,
    val relation: String = "carebuddy"
) : Parcelable

@Parcelize
data class SendSelectedInterests(
    val interests: ArrayList<String>
) : Parcelable

data class ToDoSignature(
    val patient_signature: String
)

data class CallLog(
    val client: Int,
    val contact_no: String
)

data class NotificationType(
    val type: String
)

data class ChatMsg(
    val client: String,
    val message: String,
    val chat_room: String
)

data class PrevMsg(
    val chat_room: String
)

@Parcelize
data class ForumData(
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_on: String,
    val description: String,
    val max_size: Int,
    val image: String,
    val admin: Int
) : Parcelable

@Parcelize
data class ChatRoomMsgs(
    val id: Int = 0,
    val client_name: String = "",
    val client_photo: String = "",
    val user_id: Int = 0,
    val message: String = "",
    val created_at: String = "",
    val updated_at: String = "",
    val client: Int = 0,
    val chat_room: Int = 0
) : Parcelable
