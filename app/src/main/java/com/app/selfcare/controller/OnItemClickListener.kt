package com.app.selfcare.controller

import android.view.View
import com.app.selfcare.data.Articles
import com.app.selfcare.data.Assessments
import com.app.selfcare.data.CareBuddy
import com.app.selfcare.data.CareBuddyDashboard
import com.app.selfcare.data.CareDay
import com.app.selfcare.data.CareDayIndividualTaskDetail
import com.app.selfcare.data.CarePlans
import com.app.selfcare.data.ChatRoomMsgBean
import com.app.selfcare.data.CoachType
import com.app.selfcare.data.ConsentRois
import com.app.selfcare.data.ConsentsRoisDocumentData
import com.app.selfcare.data.EventCommunity
import com.app.selfcare.data.ExerciseDashboard
import com.app.selfcare.data.ForumData
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.data.Goal
import com.app.selfcare.data.GroupAppointment
import com.app.selfcare.data.Journal
import com.app.selfcare.data.MessageBean
import com.app.selfcare.data.NutritionDashboard
import com.app.selfcare.data.Plan
import com.app.selfcare.data.Podcast
import com.app.selfcare.data.RecommendedData
import com.app.selfcare.data.Therapist
import com.app.selfcare.data.TimeSlot
import com.app.selfcare.data.ToDoData
import com.app.selfcare.data.Video

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

interface OnChatRoomMessageClickListener {
    fun onItemClick(message: ChatRoomMsgBean?)
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
    fun onToDoItemClickListener(view: View, showPopup: Boolean, status: String, toDoData: ToDoData)
}

interface OnDocumentsConsentRoisViewItemClickListener {
    fun onDocumentsConsentRoisViewItemClickListener(consentsRoisDocumentData: ConsentsRoisDocumentData)
}

interface OnEventItemClickListener {
    fun onEventItemClickListener(events: EventCommunity)
}

interface OnCareBuddyItemClickListener {
    fun onCareBuddyItemClickListener(careBuddy: CareBuddy, isCall: Boolean, isChat: Boolean)
}

interface OnCareBuddyDashboardItemClickListener {
    fun onCareBuddyDashboardItemClickListener(careBuddyDashboard: CareBuddyDashboard, isCall: Boolean, isChat: Boolean)
}

interface OnExerciseDashboardItemClickListener {
    fun onExerciseDashboardItemClickListener(exerciseDashboard: ExerciseDashboard)
}

interface OnForumItemClickListener {
    fun onForumItemClickListener(forumData: ForumData)
}

interface OnBottomReachedListener {
    fun onBottomReached(position: Int)
}