package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnDocumentsConsentRoisViewItemClickListener
import ensemblecare.csardent.com.data.ConsentsRoisDocumentData
import ensemblecare.csardent.com.databinding.LayoutItemDocumentConsentRoisBinding

class ConsentsRoisViewAdapter(
    private val context: Context,
    private val list: ArrayList<ConsentsRoisDocumentData>,
    private val adapterItemClick: OnDocumentsConsentRoisViewItemClickListener
) :
    RecyclerView.Adapter<ConsentsRoisViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConsentsRoisViewAdapter.ViewHolder {
        val binding = LayoutItemDocumentConsentRoisBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_document_consent_rois, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtDocumentViewConsentRois.text = item.name
            layoutDocumentConsentRoisView.setOnClickListener {
                adapterItemClick.onDocumentsConsentRoisViewItemClickListener(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemDocumentConsentRoisBinding) :
        RecyclerView.ViewHolder(binding.root)
}