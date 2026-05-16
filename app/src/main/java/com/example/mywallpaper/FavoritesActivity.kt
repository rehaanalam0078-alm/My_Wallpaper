package com.example.mywallpaper

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class FavoritesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_favorites)

        recyclerView =
            findViewById(R.id.favoritesRecycler)

        recyclerView.layoutManager =
            StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )

        recyclerView.adapter =
            WallpaperAdapter(
                FavoriteManager.favoriteList
            ) { wallpaper ->

                val intent =
                    Intent(
                        this,
                        PreviewActivity::class.java
                    )

                intent.putExtra(
                    "image",
                    wallpaper.imageUrl
                )

                startActivity(intent)
            }
    }
}