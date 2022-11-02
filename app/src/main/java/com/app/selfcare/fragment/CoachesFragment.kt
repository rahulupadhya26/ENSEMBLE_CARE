package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import com.app.selfcare.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_coaches.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CoachesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CoachesFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedYoga: Boolean = false
    private var selectedNutrition: Boolean = false
    private var selectedExercise: Boolean = false
    private var selectedMindfulness: Boolean = false
    private var selectedMusic: Boolean = false
    private var selectedTalk: Boolean = false
    private var selectedType: String? = null
    private var selectedCoachesTypes: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_coaches
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        // Get radio group selected item using on checked change listener
        coaches_radio_group.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = view.findViewById(checkedId)
            selectedType = if (radio.text == "Body") {
                "Body"
            } else {
                "Mind"
            }
        }

        selectedType = if (checkedBody.isChecked) {
            "Body"
        } else {
            "Mind"
        }

        layoutYoga.setOnClickListener {
            //if (!selectedMindfulness && !selectedMusic && !selectedTalk) {
            if (selectedYoga) {
                selectedYoga = false
                layoutYoga.setBackgroundResource(R.drawable.bg_time_slot_rounded)
                txtYoga.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            } else {
                selectedYoga = true
                layoutYoga.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
                txtYoga.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            }
            /*} else {
                displayMsg("Alert", "Cannot select body coaches.")
            }*/
        }

        layoutNutrition.setOnClickListener {
            //if (!selectedMindfulness && !selectedMusic && !selectedTalk) {
            if (selectedNutrition) {
                selectedNutrition = false
                layoutNutrition.setBackgroundResource(R.drawable.bg_time_slot_rounded)
                txtNutrition.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
            } else {
                selectedNutrition = true
                layoutNutrition.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
                txtNutrition.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
            }
            /*} else {
                displayMsg("Alert", "Cannot select body coaches.")
            }*/
        }

        layoutExercise.setOnClickListener {
            //if (!selectedMindfulness && !selectedMusic && !selectedTalk) {
            if (selectedExercise) {
                selectedExercise = false
                layoutExercise.setBackgroundResource(R.drawable.bg_time_slot_rounded)
                txtExercise.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
            } else {
                selectedExercise = true
                layoutExercise.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
                txtExercise.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
            }
            /*} else {
                displayMsg("Alert", "Cannot select body coaches.")
            }*/
        }

        layoutMindfulness.setOnClickListener {
            //if (!selectedYoga && !selectedNutrition && !selectedExercise) {
            if (selectedMindfulness) {
                selectedMindfulness = false
                layoutMindfulness.setBackgroundResource(R.drawable.bg_time_slot_rounded)
                txtMindfulness.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
            } else {
                selectedMindfulness = true
                layoutMindfulness.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
                txtMindfulness.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
            }
            /*} else {
                displayMsg("Alert", "Cannot select mind coaches.")
            }*/
        }

        layoutMusic.setOnClickListener {
            //if (!selectedYoga && !selectedNutrition && !selectedExercise) {
            if (selectedMusic) {
                selectedMusic = false
                layoutMusic.setBackgroundResource(R.drawable.bg_time_slot_rounded)
                txtMusic.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            } else {
                selectedMusic = true
                layoutMusic.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
                txtMusic.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            }
            /*} else {
                displayMsg("Alert", "Cannot select mind coaches.")
            }*/
        }

        layoutTalk.setOnClickListener {
            //if (!selectedYoga && !selectedNutrition && !selectedExercise) {
            if (selectedTalk) {
                selectedTalk = false
                layoutTalk.setBackgroundResource(R.drawable.bg_time_slot_rounded)
                txtTalk.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            } else {
                selectedTalk = true
                layoutTalk.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
                txtTalk.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
            }
            /*} else {
                displayMsg("Alert", "Cannot select mind coaches.")
            }*/
        }

        btnCoachesNext.setOnClickListener {
            if (selectedYoga) {
                selectedCoachesTypes.add("1")
            }
            if (selectedNutrition) {
                selectedCoachesTypes.add("2")
            }
            if (selectedExercise) {
                selectedCoachesTypes.add("3")
            }
            if (selectedMindfulness) {
                selectedCoachesTypes.add("4")
            }
            if (selectedMusic) {
                selectedCoachesTypes.add("5")
            }
            if (selectedTalk) {
                selectedCoachesTypes.add("6")
            }

            if (selectedType != null) {
                if (selectedCoachesTypes.isNotEmpty()) {
                    replaceFragment(
                        CoachesListFragment.newInstance(
                            selectedType!!,
                            Gson().toJson(selectedCoachesTypes)
                        ),
                        R.id.layout_home,
                        CoachesListFragment.TAG
                    )
                } else {
                    displayMsg("Alert", "Select the specialization")
                }
            } else {
                displayMsg("Alert", "Select the type")
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
         * @return A new instance of fragment CoachesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CoachesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Coaches"
    }
}