package com.filrouge.beemessage.utilities

import android.content.Context
import android.content.SharedPreferences
import com.filrouge.beemessage.activities.SignUpActivity

class PreferenceManager {
    constructor(applicationContext: Context?){
        sharedPreference = applicationContext!!.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    private lateinit var sharedPreference:SharedPreferences

    fun PreferenceManager(context: Context?){
        sharedPreference = context!!.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putBoolean(key:String, value:Boolean){
        var editor:SharedPreferences.Editor = sharedPreference.edit()
        editor.putBoolean(key,value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreference.getBoolean(key,false)
    }

    fun putString(key: String,value: String){
        var editor:SharedPreferences.Editor = sharedPreference.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        return sharedPreference.getString(key,null)
    }

    fun clear(){
        var editor:SharedPreferences.Editor = sharedPreference.edit()
        editor.clear()
        editor.apply()
    }

}