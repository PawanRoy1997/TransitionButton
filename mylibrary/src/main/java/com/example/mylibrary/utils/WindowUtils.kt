package com.example.mylibrary.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.WindowManager

class WindowUtils {

    private lateinit var displayMetrics: DisplayMetrics

    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    fun makeStatusbarTransparent(activity: Activity) {

        this.setWindowFlag(
            activity,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            false
        )
        activity.window.statusBarColor = Color.TRANSPARENT
    }

    fun getWidth(activity: Activity): Int {
        setupDisplayMetrics(activity)
        return displayMetrics.widthPixels
    }

    fun getHeight(activity: Activity): Int {
        setupDisplayMetrics(activity)
        return displayMetrics.heightPixels
    }

    fun getWidth(context: Context): Int {
        setupDisplayMetrics(context)
        return displayMetrics.widthPixels
    }

    fun getHeight(context: Context): Int {
        setupDisplayMetrics(context)
        return displayMetrics.heightPixels
    }

    private fun setupDisplayMetrics(activity: Activity) {
        activity.resources.displayMetrics
    }

    private fun setupDisplayMetrics(context: Context) {
        context.resources.displayMetrics
    }

}