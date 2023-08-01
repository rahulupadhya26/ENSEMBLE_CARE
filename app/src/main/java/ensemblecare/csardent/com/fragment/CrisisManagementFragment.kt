package ensemblecare.csardent.com.fragment

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.databinding.FragmentCrisisManagementBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.InternalLinkMovementMethod
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import ensemblecare.csardent.com.BuildConfig
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.databinding.DialogNumbersBinding
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CrisisManagementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CrisisManagementFragment : BaseFragment(), InternalLinkMovementMethod.OnLinkClickedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCrisisManagementBinding
    private lateinit var locationManager: LocationManager
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCrisisManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_crisis_management
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.resource_background)

        binding.txtCrisisClientName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        binding.layoutCrisisViewProfile.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                SettingsFragment(),
                R.id.layout_home,
                SettingsFragment.TAG
            )
        }

        //binding.txtCrisisCall.movementMethod = InternalLinkMovementMethod(this)
        //binding.txtCrisisLifeLine.movementMethod = InternalLinkMovementMethod(this)
        /*binding.txtCrisisTextLine.movementMethod = LinkMovementMethod.getInstance()
        binding.txtDisasterHelpline.movementMethod = InternalLinkMovementMethod(this)
        binding.txtNationalViolence.movementMethod = InternalLinkMovementMethod(this)
        binding.txtNationalChildAbuse.movementMethod = InternalLinkMovementMethod(this)
        binding.txtNationalSexualAssault.movementMethod = InternalLinkMovementMethod(this)
        binding.txtTransLifeLine.movementMethod = InternalLinkMovementMethod(this)
        binding.txtTrevorProject.movementMethod = InternalLinkMovementMethod(this)
        binding.txtElderDisability.movementMethod = InternalLinkMovementMethod(this)
        binding.txtVeteranCrisis.movementMethod = InternalLinkMovementMethod(this)*/

        /*binding.txtNationalSuicide.movementMethod = LinkMovementMethod.getInstance()
        binding.txtDisasterDistress.movementMethod = InternalLinkMovementMethod(this)
        binding.txtNationalDomestic.movementMethod = InternalLinkMovementMethod(this)
        binding.txtNationalChild.movementMethod = InternalLinkMovementMethod(this)
        binding.txtNationalSexual.movementMethod = InternalLinkMovementMethod(this)
        binding.txtTransLifeline.movementMethod = InternalLinkMovementMethod(this)
        binding.txtTheTrevorProject.movementMethod = InternalLinkMovementMethod(this)
        binding.txtVeterans.movementMethod = InternalLinkMovementMethod(this)*/

        checkLocationPermission()

        binding.btnCallYes.setOnLongClickListener {
            sendCallLog("911")
            false
        }

        binding.cardViewNationalSuicideOpen.setOnClickListener {
            openBrowser("https://988lifeline.org/")
        }

        binding.cardViewNationalSuicideCall.setOnClickListener {
            displayNumbers(true, 2, "711", "988")
        }

        binding.cardViewCrisisTextLineMsg.setOnClickListener {
            openMessagingApp("741741", "SIGNS")
        }

        binding.cardViewDisasterOpen.setOnClickListener {
            openBrowser("https://www.samhsa.gov/disaster-preparedness")
        }

        binding.cardViewDisasterMsg.setOnClickListener {
            openMessagingApp("1-800-985-5990", "")
        }

        binding.cardViewDisasterCall.setOnClickListener {
            sendCallLog("1-800-985-5990")
        }

        binding.cardViewNdvhOpen.setOnClickListener {
            openBrowser("https://www.thehotline.org/")
        }

        binding.cardViewNdvhText.setOnClickListener {
            openMessagingApp("22522", "LOVEIS")
        }

        binding.cardViewNdvhCall.setOnClickListener {
            sendCallLog("1-800-799-7233")
        }

        binding.cardViewNcahOpen.setOnClickListener {
            openBrowser("https://childhelphotline.org/")
        }

        binding.cardViewNcahText.setOnClickListener {
            openMessagingApp("1-800-422-4453", "")
        }

        binding.cardViewNcahCall.setOnClickListener {
            sendCallLog("1-800-422-4453")
        }

        binding.cardViewNsahOpen.setOnClickListener {
            openBrowser("https://rainn.org/")
        }

        binding.cardViewNsahCall.setOnClickListener {
            sendCallLog("1-800-656-4673")
        }

        binding.cardViewTransLifelineOpen.setOnClickListener {
            openBrowser("https://translifeline.org/")
        }

        binding.cardViewTransLifelineCall.setOnClickListener {
            sendCallLog("1-877-565-8860")
        }

        binding.cardViewTrevorProjectOpen.setOnClickListener {
            openBrowser("https://www.thetrevorproject.org/get-help/")
        }

        binding.cardViewTrevorProjectCall.setOnClickListener {
            sendCallLog("1-866-488-7386")
        }

        binding.cardViewEldersCall.setOnClickListener {
            sendCallLog("(800) 426-9009")
        }

        binding.cardViewVeteranOpen.setOnClickListener {
            openBrowser("https://www.veteranscrisisline.net/")
        }

        binding.cardViewVeteranText.setOnClickListener {
            openMessagingApp("838255", "")
        }

        binding.cardViewVeteranCall.setOnClickListener {
            sendCallLog("988")
        }
    }

    private fun checkLocationPermission() {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationManager = mActivity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                mActivity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    mActivity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(mActivity!!)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            if (!isGPSEnabled()) {
                enableGPS()
            } else {
                getLocation()
            }
        }
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                mActivity!!,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                mActivity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun isGPSEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
        numUpdates = 1
    }

    private fun enableGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(mActivity!!)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(
            builder.build()
        )
        task.addOnSuccessListener {
            getLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_CHECK_SETTINGS,
                        null,
                        0,
                        0,
                        0,
                        null
                    )
                    /*exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )*/
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgCrisisClient.visibility = View.VISIBLE
                binding.txtCrisisUserPic.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgCrisisClient)
            } else {
                //img_user_pic.setImageResource(R.drawable.user_pic)
                binding.imgCrisisClient.visibility = View.GONE
                binding.txtCrisisUserPic.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtCrisisUserPic.text = userTxt.substring(0, 1).uppercase()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProvider?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                updateAddress(location)
            }
        }
    }

    private fun updateAddress(location: Location) {
        try {
            val geocoder = Geocoder(requireActivity(), Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude, 1
                ) { addresses ->
                    Log.d("Location", "$addresses")
                    /*if (addresses[0].subLocality != null && addresses[0].subLocality.isNotEmpty()) {
                                        binding.txtCurrentLocation.text = addresses[0].subLocality
                                    } else if (addresses[0].thoroughfare != null && addresses[0].thoroughfare.isNotEmpty()) {
                                        binding.txtCurrentLocation.text = addresses[0].thoroughfare
                                    } else if (addresses[0].subAdminArea != null && addresses[0].subAdminArea.isNotEmpty()) {
                                        binding.txtCurrentLocation.text = addresses[0].subLocality
                                    }*/
                    binding.txtCurrentLocation.text = addresses[0].getAddressLine(0)
                }
            } else {
                val addressList = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                ) as ArrayList<Address>
                Log.d("Last Location", "$addressList")
                if (addressList.isNotEmpty()) {
                    binding.txtCurrentLocation.text = addressList[0].getAddressLine(0)
                    fusedLocationProvider!!.removeLocationUpdates(locationCallback)
                } else {
                    checkLocationPermission()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            checkLocationPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    getLocation()
                }

                Activity.RESULT_CANCELED -> {
                    enableGPS()
                }

                else -> {}
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            mActivity!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (isGPSEnabled()) {
                            enableGPS()
                        } else {
                            getLocation()
                        }
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AlertDialog.Builder(mActivity!!)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
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
                        .create()
                        .show()
                }
                return
            }
        }
        /*if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                displayMsg("Alert", "Location permission denied")
            }
        }*/
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fusedLocationProvider != null) {
            fusedLocationProvider!!.removeLocationUpdates(locationCallback)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CrisisManagementFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CrisisManagementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Crisis_Management"
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val REQUEST_CHECK_SETTINGS = 15
    }

    override fun onLinkClicked(url: String?): Boolean {
        if (!url!!.contains("http") && !url.contains("sms")) {
            sendCallLog(url)
        }
        return false
    }
}