package com.example.mywallpaper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream

class PreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var downloadBtn: Button
    private lateinit var wallpaperBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        imageView = findViewById(R.id.previewImage)

        downloadBtn = findViewById(R.id.downloadBtn)

        wallpaperBtn = findViewById(R.id.wallpaperBtn)

        val imageUrl = intent.getStringExtra("image")

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        // DOWNLOAD BUTTON

        downloadBtn.setOnClickListener {

            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {

                        saveImage(resource)
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    }
                })
        }

        // SET WALLPAPER BUTTON

        wallpaperBtn.setOnClickListener {

            Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {

                        setWallpaper(resource)
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    }
                })
        }
    }

    // SAVE IMAGE

    private fun saveImage(bitmap: Bitmap) {

        val folder = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            ).toString()
        )

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, "wallpaper_${System.currentTimeMillis()}.jpg")

        val out = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)

        out.flush()
        out.close()

        Toast.makeText(this, "Wallpaper Downloaded", Toast.LENGTH_SHORT).show()
    }

    // SET WALLPAPER

    override fun setWallpaper(bitmap: Bitmap) {

        val wallpaperManager = WallpaperManager.getInstance(this)

        wallpaperManager.setBitmap(bitmap)

        Toast.makeText(this, "Wallpaper Set Successfully", Toast.LENGTH_SHORT).show()
    }
}