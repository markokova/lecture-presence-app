package com.marko112.lecturepresenceapp

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth


        val loginButton = findViewById<Button>(R.id.activity_register_register_button)
        val registerButton = findViewById<TextView>(R.id.activity_register_login_button)

        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.activity_register_email)
            val password = findViewById<EditText>(R.id.activity_register_password)
            if(email.text.toString().isEmpty() || password.text.toString().isEmpty()){
                Toast.makeText(baseContext,"Za prijavu je potrebno ispuniti sve kutije.",Toast.LENGTH_LONG).show()
            }
            else{
                auth.signInWithEmailAndPassword(email.text.toString(),password.text.toString())
                    .addOnCompleteListener {    task ->
                        if(task.isSuccessful){
                            Toast.makeText(baseContext, "Uspješna prijava.", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        else{
                            Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Prijava neuspješna", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

}