package com.example.hadonggymapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class FavoriteLocalManager {
    private static final String PREFS_NAME = "favorite_gyms";
    private static final String KEY_FAVORITES = "favorites";

    public static void addFavorite(Context context, String gymId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> oldSet = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
        Set<String> favorites = new HashSet<>(oldSet); // Luôn tạo bản copy mới
        favorites.add(gymId);
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public static void removeFavorite(Context context, String gymId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> oldSet = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
        Set<String> favorites = new HashSet<>(oldSet);
        favorites.remove(gymId);
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public static Set<String> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
    }
} 