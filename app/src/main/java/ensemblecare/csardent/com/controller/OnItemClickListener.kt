package ensemblecare.csardent.com.controller

import android.view.View
import ensemblecare.csardent.com.data.Articles
import ensemblecare.csardent.com.data.Assessments
import ensemblecare.csardent.com.data.CareBuddy
import ensemblecare.csardent.com.data.CareBuddyDashboard
import ensemblecare.csardent.com.data.CareDay
import ensemblecare.csardent.com.data.CareDayIndividualTaskDetail
import ensemblecare.csardent.com.data.CarePlans
import ensemblecare.csardent.com.data.ChatRoomMsgBean
import ensemblecare.csardent.com.data.CoachType
import ensemblecare.csardent.com.data.ConsentRois
import ensemblecare.csardent.com.data.ConsentsRoisDocumentData
import ensemblecare.csardent.com.data.EventCommunity
import ensemblecare.csardent.com.data.ExerciseDashboard
import ensemblecare.csardent.com.data.ForumData
import ensemblecare.csardent.com.data.GetAppointment
import ensemblecare.csardent.com.data.Goal
import ensemblecare.csardent.com.data.GroupAppointment
import ensemblecare.csardent.com.data.Journal
import ensemblecare.csardent.com.data.MessageBean
import ensemblecare.csardent.com.data.NutritionDashboard
import ensemblecare.csardent.com.data.Plan
import ensemblecare.csardent.com.data.Podcast
import ensemblecare.csardent.com.data.RecommendedData
import ensemblecare.csardent.com.data.Therapist
import ensemblecare.csardent.com.data.TimeSlot
import ensemblecare.csardent.com.data.ToDoData
import ensemblecare.csardent.com.data.Video

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

interface OnCarePlanTaskViewClickListener {
    fun onCarePlanTaskViewClickListener(careDayIndividualTaskDetail: CareDayIndividualTaskDetail)
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

interface PlayerCallBack {
    fun onItemClickOnItem(albumId: Int?)
    fun onPlayingEnd()
}