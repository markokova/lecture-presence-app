package com.marko112.lecturepresenceapp

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.marko112.lecturepresenceapp.professor.SubjectDetailsProfessorFragment
import com.marko112.lecturepresenceapp.student.SubjectDetailsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MySubjectsFragment : Fragment() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var isProfessor = false

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_subjects, container, false)

        var name: String = ""
        val userSubjects = ArrayList<Subject>()
        var userSubjectIds = ArrayList<String>()
        val noSubjectsMessage = view.findViewById<TextView>(R.id.no_subjects_message)

        val recyclerView = view.findViewById<RecyclerView>(R.id.my_subjects_list)
        val recyclerAdapter = SubjectRecyclerAdapter(userSubjects)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MySubjectsFragment.activity)
            adapter = recyclerAdapter
        }

        coroutineScope.launch {
            //fetch student by ID
            auth.currentUser?.let { it ->
                try{
                    val studentDocument = db.collection("Students")
                        .document(it.uid)
                        .get().await()
                    if(studentDocument.exists()){
                        name = (studentDocument.data?.get("Name") as? String).toString()
                        userSubjectIds = getSubjectIds(studentDocument)
                    } else{
                        isProfessor = true
                    }
                }
                catch (e: Exception){
                    Log.d(TAG, "error fetching student $e")
                }

            }
            //fetch professor by ID
            auth.currentUser?.let { it ->
                try{
                    val professorDocument = db.collection("Professors")
                        .document(it.uid)
                        .get().await()
                    if(professorDocument.exists()){
                        name = (professorDocument.data?.get("Name") as? String).toString()
                        userSubjectIds = getSubjectIds(professorDocument)
                    } else{
                        isProfessor = false
                    }
                }
                catch(e: Exception){
                    Log.d(TAG, "error fetching professor $e")
                }
            }
            var userName = view.findViewById<TextView>(R.id.qr_code_subject_name)
            userName.text = name

//This is another way to get Subjects - maybe less efficient because I need to make multiple fireabase request instead of one
//            userSubjects.addAll(getSubjects2(userSubjectIds))
//            recyclerAdapter.notifyDataSetChanged()

            getSubjects(userSubjectIds){ subjects ->
                userSubjects.addAll(subjects)
                recyclerAdapter.notifyDataSetChanged()
                if(subjects.isEmpty()){
                    noSubjectsMessage.text = "Trenutno niste upisani ni na jedan kolegij."
                }
            }

            recyclerAdapter.setOnItemClickListener(listener = object: SubjectRecyclerAdapter.ItemListener{
                @SuppressLint("ResourceType")
                override fun onItemClick(index: Int) {
                    val selectedSubject = recyclerAdapter.items[index]

                    var subjectDetailsFragment: Fragment? = null
                    if(isProfessor){
                        subjectDetailsFragment = SubjectDetailsProfessorFragment()
                    } else{
                        subjectDetailsFragment = SubjectDetailsFragment()
                    }
                    val bundle = Bundle()
                    if(selectedSubject != null){
                        bundle.putString("Id", selectedSubject.id)
                        bundle.putString("Name", selectedSubject.name)
                        bundle.putString("AETotalNumber",selectedSubject.totalAENum.toString())
                        bundle.putString("LectureTotalNumber",selectedSubject.totalLectureNum.toString())
                        bundle.putString("LETotalNumber",selectedSubject.totalLENum.toString())
                        bundle.putString("CETotalNumber",selectedSubject.totalCENum.toString())
                        bundle.putString("AEDuration",selectedSubject.AEDuration.toString())
                        bundle.putString("LectureDuration",selectedSubject.LectureDuration.toString())
                        bundle.putString("LEDuration",selectedSubject.LEDuration.toString())
                        bundle.putString("CEDuration",selectedSubject.CEDuration.toString())
                    }
                    if (subjectDetailsFragment != null) {
                        subjectDetailsFragment.arguments = bundle
                        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.fragmentContainerView,subjectDetailsFragment).addToBackStack(null).commit()
                    }
                }
            })
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    //this way I make one firebase request and then I filter results to get only necessary subjects
    private fun getSubjects(subjectIds : ArrayList<String>, callback : (List<Subject>) -> Unit){
        db.collection("Subjects")
            .get()
            .addOnSuccessListener {
            val subjectList = mutableListOf<Subject>()
                for(document in it.documents){
                    if(document.id in subjectIds){
                        val subject = Subject(
                            document.id,
                            document.data?.get("Name") as String,
                            document.data?.get("TotalAENum") as Long,
                            document.data?.get("TotalLectureNum") as Long,
                            document.data?.get("TotalLENum") as Long,
                            document.data?.get("TotalCENum") as Long,
                            document.data?.get("AEDuration") as Long,
                            document.data?.get("LectureDuration") as Long,
                            document.data?.get("LEDuration") as Long,
                            document.data?.get("CEDuration") as Long
                        )
                        subjectList.add(subject)
                    }
                }
                callback(subjectList)
            }
    }

    //fetch all user subjects - this way 5-6-7 fireabase requests will be made but there is no filtering
    private suspend fun getSubjects2(userSubjectIds: ArrayList<String>): ArrayList<Subject>{
        val userSubjects = ArrayList<Subject>()

            for(subjectId in userSubjectIds){
                val subjectDocument = db.collection("Subjects")
                    .document(subjectId)
                    .get()
                    .await()
                val subject = Subject(
                    subjectId,
                    subjectDocument.data?.get("Name") as String,
                    subjectDocument.data?.get("TotalAENum") as Long,
                    subjectDocument.data?.get("TotalLectureNum") as Long,
                    subjectDocument.data?.get("TotalLENum") as Long,
                    subjectDocument.data?.get("TotalCENum") as Long,
                    subjectDocument.data?.get("AEDuration") as Long,
                    subjectDocument.data?.get("LectureDuration") as Long,
                    subjectDocument.data?.get("LEDuration") as Long,
                    subjectDocument.data?.get("CEDuration") as Long
                    )
                userSubjects.add(subject)
            }
        return userSubjects
    }

    private fun getSubjectIds(document: DocumentSnapshot): ArrayList<String>{
        val subjectIdsList = ArrayList<String>()

        document.data?.get("SubjectIds")?.let { subjectIds ->
            if (subjectIds is ArrayList<*>) {
                for (id in subjectIds) {
                    if (id is String) {
                        subjectIdsList.add(id)
                    } else {
                        subjectIdsList.add(id.toString())
                    }
                }
            } else {
                subjectIdsList.add(subjectIds.toString())
            }
        }
        return subjectIdsList
    }

}
