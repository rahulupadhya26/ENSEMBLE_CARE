package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.selfcare.R
import com.app.selfcare.utils.Utils
import kotlinx.android.synthetic.main.fragment_therapy_basic_details_a.*
import kotlinx.android.synthetic.main.fragment_therapy_selection.*
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TherapyBasicDetailsAFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapyBasicDetailsAFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedAllergies: String? = null
    private var selectedSymptoms: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapy_basic_details_a
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        allergiesSpinner()

        symptomsSpinner()

        btnBasicDetailA.setOnClickListener {
            if (selectedAllergies != null) {
                if (selectedSymptoms != null) {
                    Utils.selectedSymptoms = selectedSymptoms!!
                    Utils.allergies = selectedAllergies!!
                    replaceFragment(
                        TherapyBasicDetailsBFragment(),
                        R.id.layout_home,
                        TherapyBasicDetailsBFragment.TAG
                    )
                } else {
                    displayToast("Select the symptoms.")
                }
            } else {
                displayToast("Select the allergy.")
            }
        }
    }

    private fun allergiesSpinner() {
        val allergies = ArrayList<String>()
        allergies.add("Penicillin")
        allergies.add("Sulfa")
        allergies.add("Iodine")
        allergies.add("Codeine")
        allergies.add("Cetirizine")
        allergies.add("Ioratadine")
        allergies.add("Allegra")

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, allergies
        )
        spinnerAllergies.setAdapter(adapter)
        spinnerAllergies.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedAllergies = allergies[position]
            }
    }

    private fun symptomsSpinner() {
        val symptoms = ArrayList<String>()
        symptoms.add("Difficulty Breathing")
        symptoms.add("Fatigue")
        symptoms.add("Fever")
        symptoms.add("Headache")
        symptoms.add("Running nose")
        symptoms.add("Sore throat")
        symptoms.add("Aches")
        symptoms.add("Back pain")
        symptoms.add("Loss of appetite")

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, symptoms
        )
        spinnerSymptoms.setAdapter(adapter)
        spinnerSymptoms.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedSymptoms = symptoms[position]
            }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TherapyBasicDetailsAFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TherapyBasicDetailsAFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Basic_Details_A"
    }
}