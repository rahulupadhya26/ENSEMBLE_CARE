package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnConsentRoisItemClickListener
import ensemblecare.csardent.com.controller.OnConsentRoisViewItemClickListener
import ensemblecare.csardent.com.data.ConsentRois
import ensemblecare.csardent.com.databinding.LayoutItemConsentsRoisBinding

class ConsentRoisListAdapter(
    val list: ArrayList<ConsentRois>,
    private val adapterItemClickListener: OnConsentRoisItemClickListener?,
    private val adapterViewItemClickListener: OnConsentRoisViewItemClickListener?
) :
    RecyclerView.Adapter<ConsentRoisListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConsentRoisListAdapter.ViewHolder {
        val binding = LayoutItemConsentsRoisBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_consents_rois, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtConsentRois.text = item.text
            if (item.isCompleted) {
                txtConsentRoisStatus.setBackgroundResource(R.color.darkGreen)
                txtConsentRoisStatus.text = "Completed"
            } else {
                txtConsentRoisStatus.setBackgroundResource(R.color.red)
                txtConsentRoisStatus.text = "Incomplete"
            }

            layoutConsentRois.setOnClickListener {
                if (!item.isCompleted) {
                    val tempList: ArrayList<ConsentRois> = arrayListOf()
                    for (consentRois in list) {
                        if (!consentRois.isCompleted) {
                            tempList.add(consentRois)
                        }
                    }
                    if (tempList.isNotEmpty()) {
                        adapterItemClickListener!!.onConsentRoisItemClickListener(tempList)
                    }
                } else {
                    adapterViewItemClickListener!!.onConsentRoisViewItemClickListener(item)
                }
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemConsentsRoisBinding) :
        RecyclerView.ViewHolder(binding.root)
}