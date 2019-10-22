package ru.omfgdevelop.googleapisample

import android.content.Context

interface StorageInterface  {
     fun  getSavedTime(context: Context):Long
    fun saveLastWalkEndTime(context: Context, time:Long)
}