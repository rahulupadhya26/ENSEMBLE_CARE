package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnToDoItemClickListener
import com.app.selfcare.data.ToDoData
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_todo_list.view.*

class ToDoListAdapter(
    val context: Context,
    val keys: ArrayList<String>,
    val list: Map<String, List<ToDoData>>,
    val adapterItemClick: OnToDoItemClickListener
) :
    RecyclerView.Adapter<ToDoListAdapter.ViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ToDoListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_todo_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return keys.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ToDoListAdapter.ViewHolder, position: Int) {
        val item = keys[position]
        val toDoDate = DateUtils("$item 00:00:00")
        holder.txtToDoStartDate.text = toDoDate.getDay() + " " + toDoDate.getMonth()
        val childLayoutManager = LinearLayoutManager(
            holder.recyclerViewToDoList.context,
            LinearLayoutManager.VERTICAL,
            false
        )
        childLayoutManager.initialPrefetchItemCount = 4
        holder.recyclerViewToDoList.apply {
            layoutManager = childLayoutManager
            adapter =
                ToDoDataListAdapter(context, list[item] as ArrayList<ToDoData>, adapterItemClick)
            setRecycledViewPool(viewPool)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtToDoStartDate: TextView = itemView.txtToDoStartDate
        val recyclerViewToDoList: RecyclerView = itemView.recyclerViewToDoList
    }
}