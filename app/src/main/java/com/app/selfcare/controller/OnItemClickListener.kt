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

interface OnTherapyTypeClickListener {
    fun onTherapyTypeClickListener(therapyType: TherapyType)
}

interface AdapterCallback {
    fun onItemClicked(plan: Plan)
}

interface OnNewsItemClickListener {
    fun onNewsItemClicked(news: News)
}

interface OnPodcastItemClickListener {
    fun onPodcastItemClicked(podcast: Podcast)
}

interface OnJournalItemClickListener {
    fun onJournalItemClicked(journal: Journal, isDelete:Boolean)
}

interface OnVideoItemClickListener {
    fun onVideoItemClickListener(video: Video)
}

interface OnAppointmentItemClickListener {
    fun onAppointmentItemClickListener(appointment: Appointment, isStartAppointment:Boolean)
}