package com.app.selfcare.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.lang.reflect.Array

data class EncryptDecrypt(
    @SerializedName("msg") val data: String
)

data class Question(
    val question_id: Int,
    val question: String,
    val option_id: String,
    val next: String,
    var no_of_options: String,
    val answers: ArrayList<Answers>
)

data class Answers(
    val answer_id: Int,
    val answer: String,
    val option_id: String
)

data class OptionModel(
    val text: String,
    var isSelected: Boolean
)

data class TherapyType(
    val text: String,
    var image: Int
)

@Parcelize
data class Plan(
    var plan_id: String,
    var plan: String,
    var price_id: String,
    var price: String,
    var stripe_price_id: String
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
    val description:String,
    val years_of_experience: String,
    val street: String,
    val city: String,
    val state: String,
    val zipcode: String,
    val country: String,
    val emergency_phone: String,
    val practice_state: String,
    val license_number: String,
    val user: String
) : Parcelable

data class Login(
    val email: String,
    val password: String
)

data class DeviceId(
    val device_id: String,
    val therapy: Int,
    val consent_photo: String,
    val parent_name: String,
    val parent_relation: String,
    val parent_contact_no: String
)

data class SendAnswer(
    val anonymous_user_id: Int,
    val data: ArrayList<EachAnswer>
)

data class EachAnswer(
    val question_id: Int,
    val answer_id: Int
)

data class Employee(
    val company_name: String,
    val employee_id: String
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
    val gender: String
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
    val stripe_price_id: String
)

data class PaymentStatus(
    val id: String
)

data class PatientId(
    val patient_id: Int
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

data class HealthInfo(
    val patient: Int,
    val total_steps: Int,
    val total_distance: Int,
    val total_calaroies: Int,
    val heart_beat: Int
)

data class ProfileData(
    val patient_id: Int,
    val first_name: String,
    val middle_name: String,
    val last_name: String,
    val dob: String,
    val gender: String,
    val street: String,
    val state: String,
    val city: String,
    val county: String,
    val zipcode: String,
    val emergency_phone: String,
    val marital_status: String
)

data class PartProfileData(
    val patient_id: Int,
    val first_name: String,
    val last_name: String,
    val dob: String,
    val photo: String
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
) : Parcelable

@Parcelize
data class Podcast(
    val id: Int,
    val name: String,
    val description: String,
    val podcast_image: String,
    val artist: String,
    val podcast_url: String
) : Parcelable

@Parcelize
data class Journal(
    val id: Int,
    val name: String,
    val description: String,
    val journal_date: String,
    val month: String,
    val year: String,
    val time: String,
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
data class GetAppointment(
    val doctor_first_name: String,
    val doctor_last_name: String,
    val doctor_designation: String,
    val doctor_photo: String,
    val appointment: Appointment,
    val meeting_title: String,
    val channel_name: String,
    val rtc_token: String,
    val meeting_date: String,
    val is_group_appointment: Boolean
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
    val doctor: Int,
    val patient: Int,
    val time_slot: String,
) : Parcelable

data class TimeSlot(
    val appointment_id: String,
    val doctor_id: String,
    val date: String,
    val time_slot_start: String,
    val time_slot_end: String,
    val time_slot_id: String,
    var isSelected: Boolean
)

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
    val insurance_companhy: String,
    val plain_id: String,
    val member_id: String,
    val gourp_id: String,
    val member_name: String
)

data class Feedback(
    val patient: Int,
    val appointment: Int,
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
    val meeting_title: String,
    val channel_name: String,
    val rtc_token: String,
    val rtc_token_doctor: String,
    val meeting_date: String,
    val doctor_first_name: String,
    val doctor_last_name: String,
    val group_appointment: GroupAppointmentDetails
) : Parcelable

@Parcelize
data class GroupAppointmentDetails(
    val id: Int,
    val group_appointment_uid: String,
    val date: String,
    val duration: String,
    val time: String,
    val select_am_or_pm: String,
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
    val doctor_status: String,
    val pdf_file_url: String,
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