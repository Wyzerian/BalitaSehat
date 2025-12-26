package com.bootcamp.balitasehat.session

import android.content.Context

class ChildSession(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "CHILD_SESSION"
        private const val KEY_CHILD_ID = "child_id"
        private const val KEY_CHILD_NAME = "child_name"
    }

    // ===== SAVE =====
    fun saveChild(childId: String, childName: String) {
        if (childId.isEmpty()) return

        prefs.edit()
            .putString(KEY_CHILD_ID, childId)
            .putString(KEY_CHILD_NAME, childName)
            .apply()
    }

    // ===== GET =====
    fun getChildId(): String? {
        return prefs.getString(KEY_CHILD_ID, null)
    }

    fun getChildName(): String {
        return prefs.getString(KEY_CHILD_NAME, "-") ?: "-"
    }

    // ===== CLEAR (LOGOUT) =====
    fun clear() {
        prefs.edit().clear().apply()
    }
}
