package com.example.mywallpaper

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object FavoriteStorage {

    private const val PREF_NAME = "favorites_pref"

    private const val KEY_FAVORITES = "favorites"

    // SAVE FAVORITES

    fun saveFavorites(
        context: Context,
        favorites: ArrayList<Wallpaper>
    ) {

        val sharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val editor = sharedPreferences.edit()

        val gson = Gson()

        val json = gson.toJson(favorites)

        editor.putString(KEY_FAVORITES, json)

        editor.apply()
    }

    // LOAD FAVORITES

    fun loadFavorites(
        context: Context
    ): ArrayList<Wallpaper> {

        val sharedPreferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val gson = Gson()

        val json =
            sharedPreferences.getString(
                KEY_FAVORITES,
                null
            )

        val type =
            object : TypeToken<ArrayList<Wallpaper>>() {}.type

        return gson.fromJson(json, type)
            ?: ArrayList()
    }
}