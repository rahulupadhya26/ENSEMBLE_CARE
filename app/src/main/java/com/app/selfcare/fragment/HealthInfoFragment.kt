package com.app.selfcare.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_health_info.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

enum class FitActionRequestCode1 {
    SUBSCRIBE,
    READ_DATA
}

/**
 * A simple [Fragment] subclass.
 * Use the [HealthInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HealthInfoFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .addDataType(DataType.TYPE_DISTANCE_DELTA)
        .addDataType(DataType.TYPE_HEART_RATE_BPM)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
        .build()

    private var GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_health_info
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        checkPermissionsAndRun(FitActionRequestCode1.SUBSCRIBE)
    }

    private fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode1) {
        if (permissionApproved()) {
            fitSignIn(fitActionRequestCode)
        } else {
            requestRuntimePermissions(fitActionRequestCode)
        }
    }

    /**
     * Checks that the user is signed in, and if so, executes the specified function. If the user is
     * not signed in, initiates the sign in flow, specifying the post-sign in function to execute.
     *
     * @param requestCode The request code corresponding to the action to perform after sign in.
     */
    private fun fitSignIn(requestCode: FitActionRequestCode1) {
        if (oAuthPermissionsApproved()) {
            performActionForRequestCode(requestCode)
        } else {
            requestCode.let {
                GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    getGoogleAccount(), fitnessOptions
                )
            }
        }
    }

    /**
     * Handles the callback from the OAuth sign in flow, executing the post sign in function
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val postSignInAction = FitActionRequestCode1.values()[requestCode]
                postSignInAction.let {
                    performActionForRequestCode(postSignInAction)
                }
            }
            else -> oAuthErrorMsg(requestCode, resultCode)
        }
    }

    /**
     * Runs the desired method, based on the specified request code. The request code is typically
     * passed to the Fit sign-in flow, and returned with the success callback. This allows the
     * caller to specify which method, post-sign-in, should be called.
     *
     * @param requestCode The code corresponding to the action to perform.
     */
    private fun performActionForRequestCode(requestCode: FitActionRequestCode1) =
        when (requestCode) {
            FitActionRequestCode1.READ_DATA -> readData()
            FitActionRequestCode1.SUBSCRIBE -> subscribe()
        }

    private fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }

    private fun oAuthPermissionsApproved() =
        GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    /**
     * Gets a Google account for use in creating the Fitness client. This is achieved by either
     * using the last signed-in account, or if necessary, prompting the user to sign in.
     * `getAccountForExtension` is recommended over `getLastSignedInAccount` as the latter can
     * return `null` if there has been no sign in before.
     */
    private fun getGoogleAccount() =
        GoogleSignIn.getAccountForExtension(requireActivity(), fitnessOptions)

    /** Records step data by requesting a subscription to background step data.  */
    private fun subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(requireActivity(), getGoogleAccount())
            .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Successfully subscribed!")
                    fitSignIn(FitActionRequestCode1.READ_DATA)
                } else {
                    Log.w(TAG, "There was a problem subscribing.", task.exception)
                }
            }
    }

    private fun convertMeterToKilometer(meter: Float): Float {
        return (meter * 0.001).toFloat()
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private fun readData() {
        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
                }
                Log.i(TAG, "Total steps: $total")
                txt_steps.text = total.toString()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_DISTANCE).asFloat()
                }
                Log.i(TAG, "Total distance: $total")
                txt_distance.text =
                    String.format("%.2f", convertMeterToKilometer(total.toFloat()))
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_AVERAGE)
                }
                Log.i(TAG, "Total BPM: $total")
                txt_heart_rate.text = "$total"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { dataSet ->
                var total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_CALORIES)
                }
                if (total == null) {
                    total = ""
                }
                Log.i(TAG, "Total Calories: $total")
                txt_calories.text = "$total"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
    }

    private fun permissionApproved(): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
        return approved
    }

    private fun requestRuntimePermissions(requestCode: FitActionRequestCode1) {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    health_info_frame_layout,
                    "Permission Rationale",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("OK") {
                        // Request permission
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            1001
                        )
                    }
                    .show()
            } else {
                Log.i(TAG, "Requesting permission")
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    requestCode.ordinal
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            grantResults.isEmpty() -> {
                // If user interaction was interrupted, the permission request
                // is cancelled and you receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            }
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                // Permission was granted.
                val fitActionRequestCode = FitActionRequestCode1.values()[requestCode]
                fitActionRequestCode.let {
                    fitSignIn(fitActionRequestCode)
                }
            }
            else -> {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.

                Snackbar.make(
                    health_info_frame_layout,
                    "Permission Denied",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HealthInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HealthInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_health_info"
    }
}