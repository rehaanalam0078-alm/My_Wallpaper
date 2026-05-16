package com.example.mywallpaper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WallpaperAdapter(
    private val list: ArrayList<Wallpaper>,
    private val onClick: (Wallpaper) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView =
            view.findViewById(R.id.wallpaperImage)

        val favoriteBtn: ImageView =
            view.findViewById(R.id.favoriteBtn)
    }

    // DOUBLE TAP TIMER

    private var lastClickTime = 0L

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val currentItem = list[position]

        // LOAD IMAGE

        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)

            .placeholder(R.drawable.blur_placeholder)

            .error(R.drawable.ic_launcher_background)

            .into(holder.image)

        // FAVORITE ICON STATE

        if (currentItem.isFavorite) {

            holder.favoriteBtn.setImageResource(
                R.drawable.heart_fill
            )

        } else {

            holder.favoriteBtn.setImageResource(
                R.drawable.heart_outline
            )
        }

        // DOUBLE TAP LIKE + OPEN PREVIEW

        holder.image.setOnClickListener {

            val clickTime =
                System.currentTimeMillis()

            // DOUBLE TAP DETECT

            if (clickTime - lastClickTime < 300) {

                toggleFavorite(
                    currentItem,
                    holder,
                    position
                )
            }

            lastClickTime = clickTime

            // OPEN PREVIEW

            onClick(currentItem)
        }

        // HEART BUTTON CLICK

        holder.favoriteBtn.setOnClickListener {

            toggleFavorite(
                currentItem,
                holder,
                position
            )
        }
    }

    // FAVORITE TOGGLE FUNCTION

    private fun toggleFavorite(
        currentItem: Wallpaper,
        holder: ViewHolder,
        position: Int
    ) {

        currentItem.isFavorite =
            !currentItem.isFavorite

        // ADD FAVORITE

        if (currentItem.isFavorite) {

            if (!FavoriteManager.favoriteList.contains(currentItem)) {

                FavoriteManager.favoriteList
                    .add(currentItem)
            }

        } else {

            // REMOVE FAVORITE

            FavoriteManager.favoriteList
                .remove(currentItem)
        }

        // SAVE FAVORITES PERMANENTLY

        FavoriteStorage.saveFavorites(
            holder.itemView.context,
            FavoriteManager.favoriteList
        )

        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {

        return list.size
    }
}