package ensemblecare.csardent.com.fragment

import android.Manifest
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
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ensemblecare.csardent.com.databinding.FragmentTherapyBasicDetailsBBinding
import ensemblecare.csardent.com.utils.Utils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import ensemblecare.csardent.com.BuildConfig
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TherapyBasicDetailsBFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapyBasicDetailsBFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
        numUpdates = 1
    }
    var locationManager: LocationManager? = null
    private lateinit var binding: FragmentTherapyBasicDetailsBBinding

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
        binding = FragmentTherapyBasicDetailsBBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapy_basic_details_b
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        binding.cardViewLocateMe.setOnClickListener {
            checkLocationPermission()
        }

        binding.btnBasicDetailB.setOnClickListener {
            if (getText(binding.etBasicDetailStreet).isNotEmpty()) {
                if (getText(binding.etBasicDetailCity).isNotEmpty()) {
                    if (getText(binding.etBasicDetailState).isNotEmpty()) {
                        if (getText(binding.etBasicDetailPostalCode).isNotEmpty()) {
                            if (getText(binding.etBasicDetailCountry).isNotEmpty()) {
                                Utils.selectedStreet = getText(binding.etBasicDetailStreet)
                                Utils.selectedCity = getText(binding.etBasicDetailCity)
                                Utils.selectedState = getText(binding.etBasicDetailState)
                                Utils.selectedPostalCode = getText(binding.etBasicDetailPostalCode)
                                Utils.selectedCountry = getText(binding.etBasicDetailCountry)

                            } else {
                                setEditTextError(binding.etBasicDetailCountry,"Country cannot be blank.")
                            }
                        } else {
                            setEditTextError(binding.etBasicDetailPostalCode,"Postal code cannot be blank.")
                        }
                    } else {
                        setEditTextError(binding.etBasicDetailState,"State cannot be blank.")
                    }
                } else {
                    setEditTextError(binding.etBasicDetailCity,"City cannot be blank.")
                }
            } else {
                setEditTextError(binding.etBasicDetailStreet,"Street cannot be blank.")
            }
        }
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                updateAddressUI(location)
            }
        }
    }

    private fun checkLocationPermission() {
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(mActivity!!)
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
                fusedLocationProvider?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
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
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
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
                            fusedLocationProvider?.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.getMainLooper()
                            )
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
    }

    private fun enableGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(mActivity!!)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(
            builder.build()
        )
        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            if (ContextCompat.checkSelfPermission(
                    mActivity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationProvider?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        mActivity!!,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun updateAddressUI(location: Location) {
        val geocoder = Geocoder(mActivity!!, Locale.getDefault())

        val addressList = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        ) as ArrayList<Address>
        //tv_add.text = addressList.get(0).getAddressLine(0)
        Log.d("Last Location", "$addressList")
        if (addressList[0].subLocality != null && addressList[0].subLocality.isNotEmpty()) {
            binding.etBasicDetailStreet.setText(addressList[0].subLocality)
        } else if (addressList[0].thoroughfare != null && addressList[0].thoroughfare.isNotEmpty()) {
            binding.etBasicDetailStreet.setText(addressList[0].thoroughfare)
        } else if (addressList[0].subAdminArea != null && addressList[0].subAdminArea.isNotEmpty()) {
            binding.etBasicDetailStreet.setText(addressList[0].subAdminArea)
        }
        binding.etBasicDetailCity.setText(addressList[0].locality)
        binding.etBasicDetailState.setText(addressList[0].adminArea)
        binding.etBasicDetailPostalCode.setText(addressList[0].postalCode)
        binding.etBasicDetailCountry.setText(addressList[0].countryName)
        fusedLocationProvider!!.removeLocationUpdates(locationCallback)
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
         * @return A new instance of fragment TherapyBasicDetailsBFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TherapyBasicDetailsBFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Basic_Details_B"
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val REQUEST_CHECK_SETTINGS = 15
    }
}