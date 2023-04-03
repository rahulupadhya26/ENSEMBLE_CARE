package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnToDoItemClickListener
import com.app.selfcare.data.ToDoData
import com.app.selfcare.databinding.LayoutItemTodoDataListBinding
import com.app.selfcare.utils.DateUtils

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
        val binding = LayoutItemTodoDataListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_todo_data_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            radioButtonToDo.text = item.title
            txtToDoTitle.text = item.title
            txtToDoDesc.text = item.description
            val endDate = DateUtils(item.end_date + " 00:00:00")
            val toDoUpdatedDate = DateUtils(item.updated_on.replace("-", " "))
            /*if (DateMethods().isToday(endDate.mDate)) {
                txtToDoDate.text =
                    endDate.getDay() + " " + endDate.getMonth() + " " + endDate.getYear()
            } else {
                txtToDoDate.text =
                    toDoUpdatedDate.getDay() + " " + toDoUpdatedDate.getMonth() + " " + toDoUpdatedDate.getYear() + ", " + toDoUpdatedDate.getTime()
            }*/
            if (item.is_completed) {
                txtToDoCompleted.visibility = View.VISIBLE
                layoutCompletedToDo.visibility = View.VISIBLE
                radioButtonToDo.visibility = View.GONE
                radioButtonToDo.isChecked = true
                txtToDoDate.text =
                    toDoUpdatedDate.getDay() + " " + toDoUpdatedDate.getMonth() + " " + toDoUpdatedDate.getYear() + ", " + toDoUpdatedDate.getTime()
                radioButtonToDo.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.toDoCompletedTxtColor
                    )
                )
                txtToDoDesc.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.toDoCompletedTxtColor
                    )
                )
                txtToDoDate.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.toDoCompletedTxtColor
                    )
                )
            } else {
                txtToDoDate.text =
                    endDate.getDay() + " " + endDate.getMonth() + " " + endDate.getYear()
                txtToDoCompleted.visibility = View.GONE
                radioButtonToDo.isChecked = false
                layoutCompletedToDo.visibility = View.GONE
                radioButtonToDo.visibility = View.VISIBLE
            }

            radioButtonToDo.setOnClickListener {
                adapterItemClick.onToDoItemClickListener(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemTodoDataListBinding) :
        RecyclerView.ViewHolder(binding.root)
}