package com.example.mywallpaper.data.repository

import android.content.Context
import android.util.Log
import com.example.mywallpaper.data.model.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context, private val webClientId: String) {

    private val auth = FirebaseAuth.getInstance()
    private val rtdb = FirebaseDatabase.getInstance().reference
    private val TAG = "AuthRepository"

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
        if (webClientId.isNotBlank()) {
            gsoBuilder.requestIdToken(webClientId)
        }
        return GoogleSignIn.getClient(context, gsoBuilder.build())
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            if (idToken.isBlank()) {
                return@withContext Result.failure(Exception("Google Sign-In returned an invalid token"))
            }
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Failed to retrieve user from credential")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            if (cleanEmail.isBlank() || password.isBlank()) {
                return@withContext Result.failure(Exception("Email and password are required"))
            }
            val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, phone: String, password: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            val cleanName = name.trim()
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: throw Exception("Failed to create user account")

            // Update display name in Firebase Auth
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(cleanName)
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (pEx: Exception) {
                Log.w(TAG, "Failed to update display name: ${pEx.message}")
            }

            // Save non-sensitive profile data (NEVER store plaintext password)
            if (phone.isNotBlank()) {
                try {
                    val profileData = mapOf(
                        "uid" to user.uid,
                        "name" to cleanName,
                        "email" to cleanEmail,
                        "phoneNo" to phone.trim()
                    )
                    rtdb.child("Users").child(phone.trim()).setValue(profileData).await()
                } catch (rtdbEx: Exception) {
                    Log.w(TAG, "Failed to save profile to RTDB: ${rtdbEx.message}")
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-up error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            NotificationRepository().unregisterDeviceToken()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister FCM device token: ${e.message}")
        }
        try {
            // Explicitly sign out Google client so the account chooser dialog is displayed on next login
            getGoogleSignInClient().signOut().await()
            Log.d(TAG, "Google Sign-In client signed out successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Google sign-out failed: ${e.message}")
        }
        try {
            auth.signOut()
            Log.d(TAG, "FirebaseAuth signed out successfully")
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth sign-out failed: ${e.message}")
        }
    }

    fun getCurrentUserProfile(): UserProfile {
        val user = auth.currentUser
        return UserProfile(
            uid = user?.uid ?: "",
            name = user?.displayName ?: "Guest",
            email = user?.email ?: "",
            photoUrl = user?.photoUrl?.toString() ?: ""
        )
    }
}
