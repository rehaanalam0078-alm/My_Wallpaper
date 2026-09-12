package com.example.mywallpaper

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale



class success : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter

    private lateinit var list: ArrayList<CategoryModel>
    private lateinit var filteredList: ArrayList<CategoryModel>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_success)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // LOAD FAVORITES

        FavoriteManager.favoriteList =
            FavoriteStorage.loadFavorites(this)

        // FAVORITES PAGE

        val favpage = findViewById<ImageView>(R.id.favpage)

        favpage.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    FavoritesActivity::class.java
                )
            )
        }

        // LOGOUT BUTTON

        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        logoutBtn.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            startActivity(
                Intent(
                    this,
                    Welcome::class.java
                )
            )

            finish()
        }

        // RECYCLER VIEW

        recyclerView = findViewById(R.id.recyclerView)

        list = ArrayList()

        // ADD CATEGORIES

        list.add(CategoryModel("islamic", R.drawable.islamic))

        list.add(CategoryModel("hindusim", R.drawable.hindusim))

        list.add(CategoryModel("anime", R.drawable.anime))

        list.add(CategoryModel("kitty", R.drawable.cats_and_dogs))

        list.add(CategoryModel("cars", R.drawable.car))

        list.add(CategoryModel("nature", R.drawable.nature))

        filteredList = ArrayList(list)

        adapter = CategoryAdapter(this, filteredList)

        recyclerView.layoutManager =
            StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )

        recyclerView.adapter = adapter

        // SEARCH BAR

        val searchBar = findViewById<EditText>(R.id.searchBar)

        searchBar.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    // FILTER FUNCTION

    private fun filterData(query: String) {

        filteredList = ArrayList()

        for (item in list) {

            if (item.name.lowercase(Locale.getDefault())
                    .contains(query.lowercase(Locale.getDefault()))
            ) {

                filteredList.add(item)
            }
        }

        if (filteredList.isEmpty()) {

            Toast.makeText(
                this,
                "No Category Found",
                Toast.LENGTH_SHORT
            ).show()
        }

        adapter.filterList(filteredList)
    }
}