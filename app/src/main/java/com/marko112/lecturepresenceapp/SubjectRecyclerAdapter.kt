package com.marko112.lecturepresenceapp

import android.content.ClipData.Item
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubjectRecyclerAdapter(
     val items: ArrayList<Subject>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mListener: ItemListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SubjectViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.subject_recycler_item, parent, false), mListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is SubjectViewHolder ->{
                holder.bind(position, items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun add(item: Subject){
        items.add(item)
        notifyDataSetChanged()
    }

    class SubjectViewHolder(view: View, clickListener: ItemListener): RecyclerView.ViewHolder(view){

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }

        private val subjectName: TextView = view.findViewById(R.id.subjet_recycler_item_name)
        fun bind(index: Int, subject: Subject){
            subjectName.text = subject.name
        }
    }

    interface ItemListener{
        fun onItemClick(index: Int)
    }

    fun setOnItemClickListener(listener: ItemListener){
        mListener = listener
    }
}