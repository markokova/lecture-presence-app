package com.marko112.lecturepresenceapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val userName = findViewById<EditText>(R.id.activity_register_name)
        val userEmail = findViewById<EditText>(R.id.activity_register_email)
        val userPassword = findViewById<EditText>(R.id.activity_register_password)
        val registerButton = findViewById<Button>(R.id.activity_register_register_button)
        val goToLoginButton = findViewById<TextView>(R.id.activity_register_login_button)
        val choiceStudentProfessor = findViewById<RadioGroup>(R.id.student_professor_choice)

        //if nothing is selected the student will be created => it should check and not allow creation if every box is not checked
        var isStudent = 0
        choiceStudentProfessor.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.student_button ->{
                    isStudent = 1;
                }
                R.id.professor_button ->{
                    isStudent = 2;
                }
            }
        }

        goToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        //Mozda bi ipak trebao koristiti set i postaviti sve dokumente na user.uid iz autentifikacije
        registerButton.setOnClickListener {
            if(userName.text.isEmpty() || userEmail.text.isEmpty() || userPassword.text.isEmpty() || isStudent == 0){
                Toast.makeText(baseContext, "Potrebno je ispuniti sve kutije.", Toast.LENGTH_LONG).show()
            }else{
                auth.createUserWithEmailAndPassword(userEmail.text.toString(), userPassword.text.toString())
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful){
                            Log.d(TAG,"User created Successfully")
                            //Check if all boxes are filled
                            if(isStudent == 1) //if user is student
                            {
                                val data = hashMapOf(
                                    "Name" to userName.text.toString(),
                                    "Email" to userEmail.text.toString()
                                )
                                val attendance = hashMapOf(
                                    "AE" to 0,
                                    "Lecture" to 0,
                                    "LE" to 0,
                                    "CE" to 0
                                )
                                val subjectsAttendance = hashMapOf(
                                    "1" to attendance,
                                    "2" to attendance
                                )
                                auth.currentUser?.let { it1 ->
                                    db.collection("Students")
                                        .document(it1.uid)
                                        .set(data)
                                        .addOnSuccessListener {
                                            Log.d(TAG,"DocumentSnapshot written with ID: ${it1.uid}")
                                        }
                                        .addOnFailureListener {
                                            Log.w(TAG,"Error adding document.", it)
                                        }
                                    //adding attendance map to student
                                    db.collection("Students")
                                        .document(it1.uid)
                                        .update("Attendance", subjectsAttendance)
                                        .addOnSuccessListener {
                                            Log.d(TAG,"Attendance added to student.")
                                        }
                                }

                            }
                            else //if user is professor
                            {
                                val data = hashMapOf(
                                    "Email" to userEmail.text.toString(),
                                    "Name" to userName.text.toString(),
                                    "SubjectIds" to arrayListOf(1)
                                )
                                auth.currentUser?.let { it1 ->
                                    db.collection("Professors")
                                        .document(it1.uid)
                                        .set(data)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "DocumentSnapshot written with ID: ${it1.uid}")
                                        }
                                        .addOnFailureListener {
                                            Log.w(TAG, "Error adding document.", it)
                                        }
                                }
                            }
                            Toast.makeText(baseContext,"User is Registered.",Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))

                        }
                    }
            }

        }
    }
}