package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnToDoItemClickListener
import com.app.selfcare.data.ToDoData
import com.app.selfcare.utils.DateMethods
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_todo_data_list.view.*

class ToDoDataListAdapter(
    private val context: Context,
    private val list: ArrayList<ToDoData>,
    private val adapterItemClick: OnToDoItemClickListener
) :
    RecyclerView.Adapter<ToDoDataListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ToDoDataListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_todo_data_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ToDoDataListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.radioButtonToDo.text = item.title
        holder.txtToDoDesc.text = item.description
        val endDate = DateUtils(item.end_date + " 00:00:00")
        val toDoUpdatedDate = DateUtils(item.updated_on.replace("-", " "))
        if (DateMethods().isToday(endDate.mDate)) {
            holder.txtToDoDate.text =
                endDate.getDay() + " " + endDate.getMonth() + " " + endDate.getYear()
        } else {
            holder.txtToDoDate.text =
                toDoUpdatedDate.getDay() + " " + toDoUpdatedDate.getMonth() + " " + toDoUpdatedDate.getYear() + ", " + toDoUpdatedDate.getTime()
        }
        if (item.is_completed) {
            holder.radioButtonToDo.isChecked = true
            holder.radioButtonToDo.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.toDoCompletedTxtColor
                )
            )
            holder.txtToDoDesc.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.toDoCompletedTxtColor
                )
            )
            holder.txtToDoDate.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.toDoCompletedTxtColor
                )
            )
        } else {
            holder.radioButtonToDo.isChecked = false
        }

        holder.radioButtonToDo.setOnClickListener {
            adapterItemClick.onToDoItemClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButtonToDo: RadioButton = itemView.radioButtonToDo
        val txtToDoDesc: TextView = itemView.txtToDoDesc
        val txtToDoDate: TextView = itemView.txtToDoDate
    }
}