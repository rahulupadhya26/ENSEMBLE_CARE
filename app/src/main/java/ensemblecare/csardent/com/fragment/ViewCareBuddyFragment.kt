package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.CareBuddy
import ensemblecare.csardent.com.data.FetchCareBuddyDetail
import ensemblecare.csardent.com.databinding.FragmentViewCareBuddyBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewCareBuddyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewCareBuddyFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var careBuddy: CareBuddy? = null
    private var param2: String? = null
    private lateinit var binding: FragmentViewCareBuddyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            careBuddy = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewCareBuddyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_view_care_buddy
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        displayCareBuddyDetail()

        binding.viewCareBuddyBack.setOnClickListener {
            popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayCareBuddyDetail() {
        fetchCareBuddyDetail { response ->
            val careBuddyType: Type = object : TypeToken<ArrayList<CareBuddy?>?>() {}.type
            val careBuddyList: ArrayList<CareBuddy> =
                Gson().fromJson(response, careBuddyType)
            if (careBuddyList.isNotEmpty()) {
                val careBuddy: CareBuddy = careBuddyList[0]
                if (careBuddy.photo != null) {
                    if (careBuddy.photo.isNotEmpty()) {
                        binding.txtCareBuddyFirstLetterName.visibility = View.GONE
                        binding.imgCareBuddyImage.visibility = View.VISIBLE
                        Glide.with(requireActivity())
                            .load(BaseActivity.baseURL.dropLast(5) + careBuddy.photo)
                            .placeholder(R.drawable.events_img)
                            .transform(CenterCrop())
                            .into(binding.imgCareBuddyImage)
                    } else {
                        binding.txtCareBuddyFirstLetterName.visibility = View.VISIBLE
                        binding.txtCareBuddyFirstLetterName.text =
                            careBuddy.first_name.substring(0, 1).uppercase()
                        binding.imgCareBuddyImage.visibility = View.GONE
                    }
                } else {
                    binding.txtCareBuddyFirstLetterName.visibility = View.VISIBLE
                    binding.txtCareBuddyFirstLetterName.text =
                        careBuddy.first_name.substring(0, 1).uppercase()
                    binding.imgCareBuddyImage.visibility = View.GONE
                }
                binding.txtCareBuddyName.text = careBuddy.first_name + " " + careBuddy.last_name
                binding.txtCareBuddyRelative.text = careBuddy.relation
                binding.txtCareBuddyFullName.text = careBuddy.first_name + " " + careBuddy.last_name
                binding.txtCareBuddyGender.text = careBuddy.gender
                binding.txtCareBuddyEmail.text = careBuddy.email
                binding.txtCareBuddyPhoneNumber.text = careBuddy.phone
                binding.txtCareBuddyRela.text = careBuddy.relation
                binding.txtCareBuddyAddress1.text = careBuddy.address
                binding.txtCareBuddyAddress2.text = careBuddy.address1
                binding.txtCareBuddyCountry.text = careBuddy.country
                binding.txtCareBuddyState.text = careBuddy.state
                binding.txtCareBuddyCity.text = careBuddy.city
                binding.txtCareBuddyZipCode.text = careBuddy.zip_code
            } else {
                displayToast("No Data Found")
            }
        }
    }

    private fun fetchCareBuddyDetail(myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .fetchCareBuddyDetail(
                        "PI0069",
                        FetchCareBuddyDetail(careBuddy!!.id),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewCareBuddyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: CareBuddy, param2: String = "") =
            ViewCareBuddyFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_View_CareBuddy"
    }
}