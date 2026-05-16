package com.example.mywallpaper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignIn : AppCompatActivity() {


    lateinit var databasereference: DatabaseReference

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    companion object {
        const val KEY = "com.example.mywallpaper.signin.KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_sign_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin)) { v, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Firebase Auth
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {

            startActivity(
                Intent(this, success::class.java)
            )

            finish()
        }

        // Google Sign In Options
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )

            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInButton = findViewById<Button>(R.id.btnsignin)

        val phoneNo = findViewById<EditText>(R.id.etphone)

        val password = findViewById<EditText>(R.id.etpassword)



        // Normal Sign In
        signInButton.setOnClickListener {

            if (
                phoneNo.text.toString().isNotEmpty()
                &&
                password.text.toString().isNotEmpty()
            ) {

                readData(phoneNo.text.toString())

            } else {

                Toast.makeText(
                    this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    // Firebase Realtime Database Login
    fun readData(phoneString: String) {

        databasereference =
            FirebaseDatabase.getInstance().getReference("Users")

        databasereference.child(phoneString).get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    val dbPassword =
                        snapshot.child("password").value.toString()

                    val userEnteredPassword =
                        findViewById<EditText>(R.id.etpassword)
                            .text.toString()

                    if (userEnteredPassword == dbPassword) {

                        val intentWelcome =
                            Intent(this, success::class.java)

                        intentWelcome.putExtra(
                            "P_KEY",
                            phoneString
                        )

                        intentWelcome.putExtra(
                            "E_KEY",
                            snapshot.child("email").value.toString()
                        )

                        startActivity(intentWelcome)

                    } else {

                        Toast.makeText(
                            this,
                            "Wrong Password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {

                    Toast.makeText(
                        this,
                        "Phone number not registered",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }.addOnFailureListener {

                Toast.makeText(
                    this,
                    "Database Error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // Google Sign In Result
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account =
                    task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Firebase Google Authentication
    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential =
            GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Google Sign In Success",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(this, success::class.java)
                    )

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Google Sign In Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}