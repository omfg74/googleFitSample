package ru.omfgdevelop.googleapisample

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class SharedWorker : StorageInterface {
    val FILE_NAME:String = "saved"
    val ENTRENCE_TIME_NAME = "time"
    @SuppressLint("CommitPrefEdits")
    override fun saveLastWalkEndTime(context: Context, time: Long) {
       val prefs:SharedPreferences= context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor = prefs.edit()
        editor.putLong(ENTRENCE_TIME_NAME,time)
        editor.apply()

    }

    override fun getSavedTime(context: Context): Long {

        val prefs:SharedPreferences = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE)
        if(prefs.contains(ENTRENCE_TIME_NAME)) {
            return prefs.getLong(ENTRENCE_TIME_NAME, 0L)
        }
        return 0L
    }


}