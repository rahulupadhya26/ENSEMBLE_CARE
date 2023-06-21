package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnToDoItemClickListener
import ensemblecare.csardent.com.data.ToDoData
import ensemblecare.csardent.com.databinding.LayoutItemTodoListBinding
import ensemblecare.csardent.com.utils.DateUtils

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
        val binding =
            LayoutItemTodoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_todo_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return keys.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = keys[position]
            val toDoDate = DateUtils("$item 00:00:00")
            txtToDoStartDate.text = toDoDate.getDay() + " " + toDoDate.getMonth()
            val childLayoutManager = LinearLayoutManager(
                recyclerViewToDoList.context,
                LinearLayoutManager.VERTICAL,
                false
            )
            childLayoutManager.initialPrefetchItemCount = 4
            recyclerViewToDoList.apply {
                layoutManager = childLayoutManager
                adapter =
                    ToDoDataListAdapter(
                        context,
                        list[item] as ArrayList<ToDoData>,
                        adapterItemClick
                    )
                setRecycledViewPool(viewPool)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemTodoListBinding) :
        RecyclerView.ViewHolder(binding.root)
}