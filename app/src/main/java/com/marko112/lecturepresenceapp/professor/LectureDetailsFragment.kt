package com.marko112.lecturepresenceapp.professor

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.marko112.lecturepresenceapp.R
import com.marko112.lecturepresenceapp.Student
import com.marko112.lecturepresenceapp.student.StudentRecyclerAdapter


class LectureDetailsFragment : Fragment() {

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lecture_details, container, false)

        val lectureType = view.findViewById<TextView>(R.id.fragment_lecture_type)
        val lectureDate = view.findViewById<TextView>(R.id.fragment_lecture_date)
        val professorName = view.findViewById<TextView>(R.id.fragment_lecture_details_professor_name)
        val numOfStudents = view.findViewById<TextView>(R.id.fragment_lecture_num_students)
        val subjectTitle = view.findViewById<TextView>(R.id.fragment_lecture_subject_title)

        subjectTitle.text = arguments?.getCharSequence("subjectName").toString()
        lectureType.text = arguments?.getCharSequence("activityName").toString()
            .plus("-")
            .plus(arguments?.getCharSequence("activityNumber").toString())
        lectureDate.text = arguments?.getCharSequence("activityDate").toString()

        getProfessorName { fetchedProfessorName ->
            professorName.text = fetchedProfessorName
        }

        val studentIdList : ArrayList<String>? = arguments?.getStringArrayList("studentIds")
        numOfStudents.text = studentIdList?.size.toString()

        val noStudentsMessage = view.findViewById<TextView>(R.id.no_students_message)
        if(studentIdList?.size == 0){
            noStudentsMessage.text = "Na ovoj aktivnosti nema studenata."
        }else noStudentsMessage.text = ""

        val listOfStudents = mutableListOf<Student>()

        val recyclerView = view.findViewById<RecyclerView>(R.id.fragment_lecture_list_of_students)
        val recyclerAdapter = StudentRecyclerAdapter(listOfStudents)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LectureDetailsFragment.activity)
            adapter = recyclerAdapter
        }

        getStudentNames(studentIdList){ students ->
            listOfStudents.addAll(students)
            recyclerAdapter.notifyDataSetChanged()
        }

        return view
    }

    private fun getProfessorName(callback: (String) -> Unit){
        val professorId = arguments?.getCharSequence("professorId").toString()

        if(professorId != "" && professorId != null){
            db.collection("Professors")
                .document(professorId)
                .get()
                .addOnSuccessListener {
                    val professorName = it.get("Name").toString()
                    callback(professorName)
                }
        }
    }

    private fun getStudentNames(studentIds: ArrayList<String>?, callback: (MutableList<Student>) -> Unit){
        db.collection("Students")
            .get()
            .addOnSuccessListener {
                val fetchedStudents = mutableListOf<Student>()
                for(document in it.documents){
                    if (studentIds != null) {
                        if(document.id in studentIds){
                            val id = document.id
                            val name = document.getString("Name")
                            val email = document.getString("Email")
                            val student = Student(id, name, email)
                            fetchedStudents.add(student)
                        }
                    }
                }
                callback(fetchedStudents)
            }
    }
}