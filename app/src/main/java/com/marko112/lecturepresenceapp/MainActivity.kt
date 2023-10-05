package com.marko112.lecturepresenceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentContainerView = findViewById<FragmentContainerView>(R.id.fragmentContainerView)


        val appTitle = findViewById<TextView>(R.id.app_name_title)
        appTitle.setOnClickListener{
            //if it isn't already at home, go to home screen
            if(fragmentManager.findFragmentById(R.id.fragmentContainerView) !is WelcomeFragment){
                val fragmentTransaction = fragmentManager.beginTransaction()

                val welcomeFragment = WelcomeFragment()
                fragmentTransaction.replace(fragmentContainerView.id,welcomeFragment).commit()
            }
        }

        val fragmentTransaction = fragmentManager.beginTransaction()

        val welcomeFragment = WelcomeFragment()
        fragmentTransaction.replace(fragmentContainerView.id,welcomeFragment).commit()

    }

    override fun onBackPressed() {
        if(fragmentManager.findFragmentById(R.id.fragmentContainerView) is WelcomeFragment){
            finishAffinity()
        }else{
            supportFragmentManager.popBackStack()
        }
    }

}


//db.collection("students")
//            .get()
//            .addOnSuccessListener {
//                for(data in it.documents){
//                    val student = data.toObject(Student::class.java)
//                    if (student?.email == auth.currentUser?.uid){
//                        val bundle = Bundle()
//                        //Can I actually access this data with student variable, what will the id be?
//                        bundle.putString("name",student?.name)
//                        bundle.putString("email",student?.email)
//                        bundle.putString("id",student?.id)
//                        bundle.putString("attendance", student?.attendance.toString())
//                        fragmentTransaction.commit()
//                    }
//                }
//            }

//        if(auth.currentUser?.email == )
