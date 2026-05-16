package com.example.mywallpaper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class success : AppCompatActivity() {

    private lateinit var logoutBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_success)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        logoutBtn = findViewById(R.id.logoutBtn)
        logoutBtn.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            startActivity(
                Intent(this, Welcome::class.java)
            )

            finish()
        }


        FavoriteManager.favoriteList =
            FavoriteStorage.loadFavorites(this)

        val favpage = findViewById<Button>(R.id.favpage)
        favpage.setOnClickListener {
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }



        val anmiecard= findViewById<CardView>(R.id.animecard)
        val carcard= findViewById<CardView>(R.id.carcard)
        val naturecard= findViewById<CardView>(R.id.naturecard)
        val islamiccard = findViewById<CardView>(R.id.islamiccard)

        anmiecard.setOnClickListener {

            val intent = Intent(this, WallpaperActivity::class.java)
            intent.putExtra("category", "anime")
            startActivity(intent)
        }
        carcard.setOnClickListener {

            val intent = Intent(this, WallpaperActivity::class.java)
            intent.putExtra("category", "cars")
            startActivity(intent)
        }
        naturecard.setOnClickListener {

            val intent = Intent(this, WallpaperActivity::class.java)
            intent.putExtra("category", "nature")
            startActivity(intent)
        }
        islamiccard.setOnClickListener {

            val intent = Intent(this, WallpaperActivity::class.java)
            intent.putExtra("category", "islamic")
            startActivity(intent)
        }



    }
}