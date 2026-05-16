package com.example.mywallpaper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.internal.Objects
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val signupButton = findViewById<Button>(R.id.btnsignup)
        val etname = findViewById<EditText>(R.id.etname)
        val etemail= findViewById<EditText>(R.id.etemail)
        val userphoneNO= findViewById<EditText>(R.id.etphone)
        val userpassword= findViewById<EditText>(R.id.etpassword)

        signupButton.setOnClickListener {
            val name = etname.text.toString()
            val email = etemail.text.toString()
            val phoneNo = userphoneNO.text.toString()
            val password= userpassword.text.toString()

            val  user= user(name, email, phoneNo,password )
            database= FirebaseDatabase.getInstance().getReference("Users")

            database.child(phoneNo).setValue(user).addOnSuccessListener {
                etname.text.clear()
                etemail.text.clear()
                userphoneNO.text.clear()
                userpassword.text.clear()

                Toast.makeText(this,"User Registered",
                Toast.LENGTH_SHORT).show() }.addOnFailureListener { Toast.makeText(this,"Failed",
                Toast.LENGTH_SHORT).show() }




             }

    }
}
