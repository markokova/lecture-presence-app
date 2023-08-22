package com.marko112.lecturepresenceapp.professor

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.marko112.lecturepresenceapp.Lecture
import com.marko112.lecturepresenceapp.LectureRecyclerAdapter
import com.marko112.lecturepresenceapp.R
import com.marko112.lecturepresenceapp.SubjectRecyclerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class LectureHistoryFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_lecture_history, container, false)

        val subjectTitle = view.findViewById<TextView>(R.id.fragment_lecture_history_title)
        subjectTitle.text = arguments?.getCharSequence("subjectName").toString()

        var lectureList = mutableListOf<Lecture>()

        val recyclerView = view.findViewById<RecyclerView>(R.id.history_lecture_list)
        val recyclerAdapter = LectureRecyclerAdapter(lectureList)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LectureHistoryFragment.activity)
            adapter = recyclerAdapter
        }

        getLectures { lectures ->
            lectureList.addAll(lectures) // Add fetched lectures to the list
            recyclerAdapter.notifyDataSetChanged()

            val noLecturesMessage = view.findViewById<TextView>(R.id.no_lectures_message)
            if(lectureList.size == 0){
                noLecturesMessage.text = "Nema niti jedne kreirane aktivnosti."
            }else{ noLecturesMessage.text = "" }
        }



        recyclerAdapter.setOnItemClickListener(listener = object: LectureRecyclerAdapter.ItemListener{
            override fun onItemClick(index: Int){
                val selectedLecture = recyclerAdapter.items[index]
                val bundle = Bundle()
                bundle.putString("activityDate",selectedLecture.date)
                bundle.putString("activityName",selectedLecture.type)
                //bundle.putString("activityType", arguments?.getCharSequence("activityType").toString())
                bundle.putString("activityNumber", selectedLecture.number.toString())
                bundle.putStringArrayList("studentIds",ArrayList(selectedLecture.studentIds))
                //bundle.putString("activityId",selectedLecture.id)
                //bundle.putString("subjectId",arguments?.getCharSequence("subjectId").toString())
                bundle.putString("subjectName", arguments?.getCharSequence("subjectName").toString())
                bundle.putString("professorId", selectedLecture.professorId)
                //bundle.putString("activityName", arguments?.getCharSequence("activityName").toString())

                val lectureDetailsFragment = LectureDetailsFragment()
                val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()

                lectureDetailsFragment.arguments = bundle
                fragmentTransaction.replace(R.id.fragmentContainerView, lectureDetailsFragment).addToBackStack(null).commit()
            }
        })

        return view
    }


    private fun getLectures(callback: (List<Lecture>) -> Unit) {
        val subjectId = arguments?.getCharSequence("subjectId").toString()
        val activityType = arguments?.getCharSequence("activityType").toString()
        val activityName = arguments?.getCharSequence("activityName").toString()

        db.collection("Subjects")
            .document(subjectId)
            .collection(activityType)
            .get()
            .addOnSuccessListener { it ->
                val fetchedLectures = mutableListOf<Lecture>()
                for(document in it.documents){
                    val id = document?.id
                    val activityNumber = document?.getLong("ActivityNumber")?.toInt()
                    val date = document?.getString("Date")
                    val professorId = document?.getString("ProfessorId")
                    val studentIds = document?.get("StudentIds") as? MutableList<String> ?: mutableListOf()

                    val lecture = Lecture(id, date, activityName, activityNumber, professorId, studentIds)
                    fetchedLectures.add(lecture)
                }
                val sortedLectures = fetchedLectures.sortedBy { it.number }
                callback(sortedLectures)
            }
    }
}