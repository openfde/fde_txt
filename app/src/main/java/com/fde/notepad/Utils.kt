package com.fde.notepad

import android.util.Log
import android.view.View

class Utils {
    companion object {
        private val TAG = "Utils"
        val ALL_INVISIBLE = 0
        val FILE_VISIBLE = 1
        val EDIT_VISIBLE = 2
        val ALL_VISIBLE = (FILE_VISIBLE + EDIT_VISIBLE)
        fun isTouchPointInView(view: View, x: Int, y: Int): Boolean {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + view.width
            val bottom = top + view.height
//            Log.w(TAG, "left = $left, right = $right, top = $top, bottom = $bottom")
//            Log.w(TAG, "x = $x, y = $y")
            return y in top..bottom && x in left..right
        }
    }
}