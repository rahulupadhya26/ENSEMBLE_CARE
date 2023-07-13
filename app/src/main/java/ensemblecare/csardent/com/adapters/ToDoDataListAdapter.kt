package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnToDoItemClickListener
import ensemblecare.csardent.com.data.ToDoData
import ensemblecare.csardent.com.databinding.LayoutItemTodoDataListBinding
import ensemblecare.csardent.com.utils.DateUtils

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
            if (item.description != null) {
                txtToDoDesc.visibility = View.VISIBLE
                txtToDoDesc.text = item.description
            } else {
                txtToDoDesc.visibility = View.GONE
            }
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
                layoutNotCompletedToDo.visibility = View.GONE
                radioButtonToDo.isChecked = true
                txtToDoDate.text =
                    toDoUpdatedDate.getMonth() + " " + toDoUpdatedDate.getDay() + " " + toDoUpdatedDate.getYear() + ", " + toDoUpdatedDate.getTime()
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
                    endDate.getMonth() + " " + endDate.getDay() + " " + endDate.getYear()
                txtToDoCompleted.visibility = View.GONE
                radioButtonToDo.isChecked = false
                layoutCompletedToDo.visibility = View.GONE
                layoutNotCompletedToDo.visibility = View.VISIBLE
            }

            if (item.is_assign == "True") {
                imgToDoDeleteMenu.visibility = View.GONE
                imgToDoMenu.visibility = View.GONE
                if (item.document != null) {
                    layoutViewDocument.visibility = View.VISIBLE
                }
            } else {
                imgToDoDeleteMenu.visibility = View.VISIBLE
                imgToDoMenu.visibility = View.VISIBLE
                layoutViewDocument.visibility = View.GONE
            }

            radioButtonToDo.setOnClickListener {
                adapterItemClick.onToDoItemClickListener(radioButtonToDo, false, "", item)
            }

            layoutViewDocument.setOnClickListener {
                adapterItemClick.onToDoItemClickListener(
                    layoutViewDocument,
                    false,
                    "Document",
                    item
                )
            }

            imgToDoDeleteMenu.setOnClickListener {
                adapterItemClick.onToDoItemClickListener(
                    imgToDoDeleteMenu,
                    true,
                    "Incomplete",
                    item
                )
            }

            imgToDoMenu.setOnClickListener {
                adapterItemClick.onToDoItemClickListener(imgToDoMenu, true, "Complete", item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemTodoDataListBinding) :
        RecyclerView.ViewHolder(binding.root)
}