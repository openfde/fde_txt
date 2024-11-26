package com.fde.txt.provider

import android.view.View

class Utils {
    companion object {
        private val TAG = "Utils"
        private val PREFIX = "primary:"
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
            return y in top..bottom && x in left..right
        }

        fun getTitle(path: String?): String {
            try {
                if (path == null) return ""
                val start = maxOf(path.indexOf(PREFIX) + (PREFIX.length), path.lastIndexOf('/') + 1)
                val end = maxOf(path.lastIndexOf('.'))
                return path.substring(start, end)
            } catch (e: Exception) {
                e.printStackTrace();
            }
            return  "";
        }

//        fun save(activity: AppCompatActivity, content: String) {
//            activity.registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
//                uri?.let {
//                    activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
//                        outputStream.write(content.toByteArray())
//                    }
//                }
//            }.launch(".txt")
//        }
    }
}