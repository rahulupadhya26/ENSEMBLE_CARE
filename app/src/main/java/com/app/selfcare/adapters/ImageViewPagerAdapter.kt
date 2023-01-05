package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class ImageViewPagerAdapter(
    private val context: Context,
    private var imageList: ArrayList<String>
) : PagerAdapter() {

    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View =
            (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.display_image_item, null
            )
        val ivImages = view.findViewById<ImageView>(R.id.displayImageView)

        Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + imageList[position])
            .transform(CenterCrop(), RoundedCorners(5))
            .into(ivImages)


        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val view = `object` as View
        vp.removeView(view)
    }
}