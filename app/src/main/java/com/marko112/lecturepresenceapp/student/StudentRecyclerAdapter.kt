package com.marko112.lecturepresenceapp.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.marko112.lecturepresenceapp.R
import com.marko112.lecturepresenceapp.Student

class StudentRecyclerAdapter(
    val items: MutableList<Student>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return StudentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.student_recycler_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is StudentViewHolder ->{
                holder.bind(position, items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val studentName : TextView = view.findViewById<TextView>(R.id.student_recycler_item_student_name)

        fun bind(index: Int, student: Student){
            studentName.text = student.name
        }
    }

}