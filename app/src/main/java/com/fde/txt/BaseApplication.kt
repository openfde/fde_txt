package com.fde.txt

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class BaseApplication : Application() {
    // application exists globally with the application and therefore does not cause memory leaks
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}