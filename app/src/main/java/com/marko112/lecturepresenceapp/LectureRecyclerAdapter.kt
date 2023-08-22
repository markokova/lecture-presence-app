package com.marko112.lecturepresenceapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LectureRecyclerAdapter(
    val items: MutableList<Lecture>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mListener: ItemListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LectureViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.lecture_recycler_item, parent, false), mListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is LectureViewHolder -> {
                holder.bind(position, items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class LectureViewHolder(view: View, clickListener: ItemListener) : RecyclerView.ViewHolder(view){

        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }
        }

        private val lectureTitle: TextView = view.findViewById<TextView>(R.id.lecture_recycler_item_lecture_name)
        private val lectureDate: TextView = view.findViewById<TextView>(R.id.lecture_recycler_item_date)

        fun bind(index: Int, lecture: Lecture){
            lectureTitle.text = "${lecture.type} - ${lecture.number}"
            lectureDate.text = lecture.date
        }
    }

    interface ItemListener{
        fun onItemClick(index: Int)
    }

    fun setOnItemClickListener(listener: LectureRecyclerAdapter.ItemListener){
        mListener = listener
    }
}

