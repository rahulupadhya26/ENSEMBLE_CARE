package com.app.selfcare.fragment

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.R
import com.app.selfcare.adapters.NewsSliderAdapter
import com.app.selfcare.adapters.NutritionSliderAdapter
import com.app.selfcare.data.Nutrition
import kotlinx.android.synthetic.main.fragment_nutrition.*
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NutritionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NutritionFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var sliderHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_nutrition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        nutritionBack.setOnClickListener {
            popBackStack()
        }

        val nutritionList: ArrayList<Nutrition> = arrayListOf()
        nutritionList.add(Nutrition("Fruit Salad Bowl", R.drawable.nutrition_img1))
        nutritionList.add(Nutrition("Morning Breakfast", R.drawable.nutrition_img2))

        viewPagerNutritionSlider.adapter =
            NutritionSliderAdapter(
                mActivity!!,
                nutritionList, viewPagerNutritionSlider
            )

        viewPagerNutritionSlider.clipToPadding = false
        viewPagerNutritionSlider.clipChildren = false
        viewPagerNutritionSlider.offscreenPageLimit = 3
        viewPagerNutritionSlider.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransform = CompositePageTransformer()
        compositePageTransform.addTransformer(MarginPageTransformer(0))
        compositePageTransform.addTransformer { page, position ->
            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPagerNutritionSlider.setPageTransformer(compositePageTransform)

        viewPagerNutritionSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000)
            }
        })
    }

    private val sliderRunnable: Runnable = Runnable {
        viewPagerNutritionSlider.currentItem = viewPagerNutritionSlider.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NutritionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NutritionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Nutrition"
    }
}