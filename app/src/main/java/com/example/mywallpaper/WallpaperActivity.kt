package com.example.mywallpaper

import android.content.Intent
import android.os.Bundle

import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class WallpaperActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var wallpaperList: ArrayList<Wallpaper>
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)

        recyclerView = findViewById(R.id.recyclerView)

        wallpaperList = ArrayList()

        db = FirebaseFirestore.getInstance()

        val category = intent.getStringExtra("category")

        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )

        staggeredGridLayoutManager.gapStrategy =
            StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        recyclerView.layoutManager = staggeredGridLayoutManager

        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(5)
        )


        db.collection("wallpapers")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { result ->
                println("Documents found: ${result.size()}")

                wallpaperList.clear()

                for (document in result) {
                    println(document.data)

                    val wallpaper =
                        document.toObject(Wallpaper::class.java)

                    wallpaperList.add(wallpaper)
                }

                recyclerView.adapter =
                    WallpaperAdapter(wallpaperList) { wallpaper ->

                        val intent =
                            Intent(this, PreviewActivity::class.java)

                        intent.putExtra("image", wallpaper.imageUrl)

                        startActivity(intent)
                    }
            }
    }
}

