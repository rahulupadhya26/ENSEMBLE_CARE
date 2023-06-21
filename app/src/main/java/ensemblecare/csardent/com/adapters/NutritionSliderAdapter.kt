package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.controller.OnNutritionDashboardItemClickListener
import ensemblecare.csardent.com.data.NutritionDashboard
import ensemblecare.csardent.com.databinding.LayoutItemNutritionSliderBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class NutritionSliderAdapter(
    val context: Context,
    var list: ArrayList<NutritionDashboard>,
    val viewPager: ViewPager2,
    private val adapterItemClickListener: OnNutritionDashboardItemClickListener?
) :
    RecyclerView.Adapter<NutritionSliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(val binding: LayoutItemNutritionSliderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = LayoutItemNutritionSliderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_nutrition_slider, parent, false)*/
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            Glide.with(context)
                .load(BaseActivity.baseURL.dropLast(5) + item.image)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imgNutrition)
            textNutrition.text = item.nutrition_name
            if (position == list.size - 2) {
                viewPager.post(runnable)
            }
            layoutNutritionSliderPane.setOnClickListener {
                adapterItemClickListener!!.onNutritionDashboardItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    val runnable: Runnable = Runnable {
        list.addAll(list)
        notifyDataSetChanged()
    }
}