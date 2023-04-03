package com.app.selfcare.controller

import com.app.selfcare.data.*

interface OnItemTherapistImageClickListener {
    fun onItemTherapistImageClickListener(therapist: Therapist)
}

interface OnTherapistItemClickListener {
    fun onTherapistItemClickListener(therapist: Therapist)
}

interface OnClickListener {
    fun onClickListener(string: String, isRemove: Boolean)
}

interface OnTextClickListener {
    fun onTextClickListener(timeSlot: TimeSlot)
}

interface OnCoachTypeClickListener {
    fun onCoachTypeClickListener(coachType: CoachType)
}

interface AdapterCallback {
    fun onItemClicked(plan: Plan)
}

interface OnNewsItemClickListener {
    fun onNewsItemClicked(articles: Articles)
}

interface OnPodcastItemClickListener {
    fun onPodcastItemClicked(podcast: Podcast)
}

interface OnJournalItemClickListener {
    fun onJournalItemClicked(journal: Journal, isDelete: Boolean)
}

interface OnVideoItemClickListener {
    fun onVideoItemClickListener(video: Video, isFav: Boolean, isWellness: Boolean)
}

interface OnGoalItemClickListener {
    fun onGoalItemClickListener(goal: Goal, isDelete: Boolean)
}

interface OnAppointmentItemClickListener {
    fun onAppointmentItemClickListener(appointment: GetAppointment, isStartAppointment: Boolean)
}

interface OnGroupAppointmentItemClickListener {
    fun onGroupAppointmentItemClickListener(groupAppointment: GroupAppointment)
}

interface OnAssessmentItemClickListener {
    fun onAssessmentItemClickListener(assessment: Assessments, type: String, isView: Boolean)
}

interface OnMessageClickListener {
    fun onItemClick(message: MessageBean?)
}

interface OnChatMessageClickListener {
    fun onItemClick(message: ChatMessages?)
}

interface OnRecommendedItemClickListener {
    fun onRecommendedItemClickListener(recommendedData: RecommendedData)
}

interface OnConsentRoisItemClickListener {
    fun onConsentRoisItemClickListener(consentRois: ArrayList<ConsentRois>)
}

interface OnConsentRoisViewItemClickListener {
    fun onConsentRoisViewItemClickListener(consentRois: ConsentRois)
}

interface OnNutritionDashboardItemClickListener {
    fun onNutritionDashboardItemClicked(nutritionDashboard: NutritionDashboard)
}

interface OnCarePlanDayWiseItemClickListener {
    fun onCarePlanDayWiseItemClickListener(carePlans: CarePlans, careDay: CareDay)
}

interface OnCarePlanDayItemClickListener {
    fun onCarePlanDayItemClickListener(dayNumber: Int)
}

interface OnCarePlanTaskItemClickListener {
    fun onCarePlanTaskItemClickListener(type: String)
}

interface OnCarePlanPendingTaskItemClickListener {
    fun onCarePlanPendingTaskItemClickListener(
        careDayIndividualTaskDetail: CareDayIndividualTaskDetail,
        isCompleted: Boolean
    )
}

interface OnDocumentItemClickListener {
    fun onDocumentItemClickListener(imageList: ArrayList<String>, title: String)
}

interface OnToDoItemClickListener {
    fun onToDoItemClickListener(toDoData: ToDoData)
}

interface OnDocumentsConsentRoisViewItemClickListener {
    fun onDocumentsConsentRoisViewItemClickListener(consentsRoisDocumentData: ConsentsRoisDocumentData)
}

interface OnEventItemClickListener {
    fun onEventItemClickListener(events: EventCommunity)
}

interface OnCareBuddyItemClickListener {
    fun onCareBuddyItemClickListener(careBuddy: CareBuddy)
}