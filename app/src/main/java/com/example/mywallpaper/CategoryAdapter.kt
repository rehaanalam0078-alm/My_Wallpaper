package com.example.mywallpaper

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val context: Context,
    private var list: ArrayList<CategoryModel>
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.categoryImage)

        val name: TextView =
            itemView.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(context)
            .inflate(R.layout.category_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val model = list[position]

        holder.name.text = model.name

        holder.image.setImageResource(model.image)

        // CLICK EVENT

        holder.itemView.setOnClickListener {

            val intent = Intent(
                context,
                WallpaperActivity::class.java
            )

            intent.putExtra(
                "category",
                model.name
            )

            context.startActivity(intent)
        }
    }

    // SEARCH FILTER FUNCTION

    fun filterList(filteredList: ArrayList<CategoryModel>) {

        list = filteredList

        notifyDataSetChanged()
    }
}