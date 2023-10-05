package com.marko112.lecturepresenceapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class WelcomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private var userName = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        val userNameTextView = view.findViewById<TextView>(R.id.user_name_welcome)
        val mySubjectsButton = view.findViewById<Button>(R.id.my_subjects_button)
        val logoutButton = view.findViewById<ImageButton>(R.id.logout_button)

        CoroutineScope(Dispatchers.Main).launch {
            userName = getName()
            userNameTextView.text = userName
        }

        mySubjectsButton.setOnClickListener {
            val mySubjectsFragment = MySubjectsFragment()
            val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainerView,mySubjectsFragment).addToBackStack(null).commit()
        }

        logoutButton.setOnClickListener{
            auth.signOut()
            startActivity(Intent(this.context, LoginActivity::class.java))

        }

        return view
    }

//    private suspend fun getName(): String{
//        var name = ""
//        auth.currentUser?.let {
//            val studentDocument = db.collection("Students")
//                .document(it.uid)
//                .get()
//                .await()
//            val studentName = studentDocument.get("Name")
//            if(studentName != null && studentName != ""){
//                name = studentName.toString()
//            }else{
//                val professorDocument = db.collection("Professors")
//                    .document(it.uid)
//                    .get()
//                    .await()
//
//                val professorName = professorDocument.getString("Name")
//                if(professorName != null && professorName != ""){
//                    name = professorName.toString()
//                }
//            }
//        }
//
//        return name
//    }

    private suspend fun getName(): String{
        var name = ""
        auth.currentUser?.let {
            val professorDocument = db.collection("Professors")
                .document(it.uid)
                .get()
                .await()
            val professorName = professorDocument.getString("Name")
            if(professorName != null && professorName != ""){
                name = professorName.toString()
            }
            else{
                val studentDocument = db.collection("Students")
                    .document(it.uid)
                    .get()
                    .await()
                val studentName = studentDocument.get("Name")
                if(studentName != null && studentName != ""){
                    name = studentName.toString()
                }
            }
        }

        return name
    }

}