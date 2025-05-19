package com.km.expense.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.km.expense.ConstructionSiteManagerApp

class UserPreferences {
    private val sharedPreferences: SharedPreferences by lazy {
        ConstructionSiteManagerApp.instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveUserId(userId: String) {
        sharedPreferences.edit {
            putString(KEY_USER_ID, userId)
        }
    }
    
    fun getUserId(): String {
        return sharedPreferences.getString(KEY_USER_ID, "") ?: ""
    }
    
    fun saveUserEmail(email: String) {
        sharedPreferences.edit {
            putString(KEY_USER_EMAIL, email)
        }
    }
    
    fun getUserEmail(): String {
        return sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
    }
    
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        }
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun setFirstLaunch(isFirstLaunch: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_FIRST_LAUNCH, isFirstLaunch)
        }
    }
    
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }
    
    fun clearUserData() {
        sharedPreferences.edit {
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }
    }
    
    companion object {
        private const val PREF_NAME = "construction_site_manager_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    }
}
