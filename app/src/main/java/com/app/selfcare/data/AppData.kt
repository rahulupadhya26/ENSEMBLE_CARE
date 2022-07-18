package com.app.selfcare.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class Question(
    val question_id: Int,
    val question: String,
    var no_of_options: String,
    var option_1: OptionData,
    var option_2: OptionData,
    var option_3: OptionData,
    var option_4: OptionData
)

data class OptionData(
    var answer_id: Int?,
    var answer: String?
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
    val dob: String,
    val qualification: String,
    val years_of_experience: String,
    val street: String,
    val city: String,
    val state: String
) : Parcelable

data class Login(
    val email: String,
    val password: String
)

data class DeviceId(
    val device_id: String,
    val therapy: Int
)

data class SendAnswer(
    val anonymous_user_id: Int,
    val question_id: Int,
    val answer_id: Int
)

data class Employee(
    val company_name: String,
    val employee_id: String
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
    val ssn: String,
    val dob: String,
    val device_id: String,
    val is_employee: Boolean,
    val employer: String,
    val employee_id: String
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
    val customer_id: String,
    val stripe_price_id: String
)

data class PaymentStatus(
    val id: String
)

data class PatientId(
    val patient_id: Int
)

data class TimeSlots(
    val doctor_public_id: String,
    val date: String
)

data class AppointmentReq(
    val appointment_id: String,
    val patient: String,
    val is_booked: Boolean,
    val booking_date: String,
    val type_of_visit: String,
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
data class News(
    val newsTitle: String,
    val newsLink: String,
    val newsPublishOn: String,
) : Parcelable

@Parcelize
data class Podcast(
    val title: String,
    val image: String,
    val artist: String,
    val audio: String
) : Parcelable

@Parcelize
data class Journal(
    val title: String,
    val desc: String,
    val date: String,
    val month: String,
    val year: String,
    val time: String
) : Parcelable

@Parcelize
data class Video(
    val title: String,
    val desc: String,
    val link: String,
    val banner: String,
    val date
    : String,
    val time: String
) : Parcelable

@Parcelize
data class Appointment(
    val total: String,
    val tele_token: String,
    val appt_id: String,
    val pc_eid: String,
    val meeting_mode: String,
    val provider_id: String,
    val provider_type: String,
    val pc_title: String,
    val pc_eventDate: String,
    val pc_startTime: String,
    val consent: String,
    val pc_endTime: String,
    val pc_duration: String,
    val pc_apptstatus: String,
    val fname: String,
    val lname: String,
    val phonecell: String,
    val physician_type: String,
    val email: String
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