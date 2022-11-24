package com.app.selfcare.controller

import com.app.selfcare.data.*

interface OnItemTherapistImageClickListener {
    fun onItemTherapistImageClickListener(therapist: Therapist)
}

interface OnTherapistItemClickListener {
    fun onTherapistItemClickListener(therapist: Therapist)
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
    fun onVideoItemClickListener(video: Video)
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

interface OnRecommendedItemClickListener {
    fun onRecommendedItemClickListener(recommendedData: RecommendedData)
}

interface OnConsentRoisItemClickListener {
    fun onConsentRoisItemClickListener(consentRois: ConsentRois)
}