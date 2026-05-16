package com.david.chatsafe.utils

import android.content.Context
import android.content.SharedPreferences

object BlockedUserRepo {
    private const val PREF_NAME = "ChatSafe_Blocked_Users"
    private const val KEY_NAMES = "blocked_names_list"

    private const val PREFS_NAME = "ChatSafePrefs"
    private const val KEY_MESSENGER_NAMES = "blocked_names"
    private const val KEY_WHATSAPP_NAMES = "whatsapp_blocked_names"
    private const val KEY_INSTAGRAM_NAMES = "instagram_blocked_names" // NUEVA LLAVE

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // --- LÓGICA DE MESSENGER ---
    fun addName(context: Context, name: String) {
        val currentList = getBlockedNames(context).toMutableSet()
        currentList.add(name.trim())
        getPrefs(context).edit().putStringSet(KEY_NAMES, currentList).apply()
    }

    fun removeName(context: Context, name: String) {
        val currentList = getBlockedNames(context).toMutableSet()
        currentList.remove(name)
        getPrefs(context).edit().putStringSet(KEY_NAMES, currentList).apply()
    }

    fun getBlockedNames(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_NAMES, emptySet()) ?: emptySet()
    }

    // --- LÓGICA DE WHATSAPP ---
    fun getWhatsAppNames(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_WHATSAPP_NAMES, emptySet()) ?: emptySet()
    }

    fun addWhatsAppName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val names = getWhatsAppNames(context).toMutableSet()
        names.add(name)
        prefs.edit().putStringSet(KEY_WHATSAPP_NAMES, names).apply()
    }

    fun removeWhatsAppName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val names = getWhatsAppNames(context).toMutableSet()
        names.remove(name)
        prefs.edit().putStringSet(KEY_WHATSAPP_NAMES, names).apply()
    }

    // --- NUEVA LÓGICA DE INSTAGRAM ---
    fun getInstagramNames(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_INSTAGRAM_NAMES, emptySet()) ?: emptySet()
    }

    fun addInstagramName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val names = getInstagramNames(context).toMutableSet()
        names.add(name)
        prefs.edit().putStringSet(KEY_INSTAGRAM_NAMES, names).apply()
    }

    fun removeInstagramName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val names = getInstagramNames(context).toMutableSet()
        names.remove(name)
        prefs.edit().putStringSet(KEY_INSTAGRAM_NAMES, names).apply()
    }

}