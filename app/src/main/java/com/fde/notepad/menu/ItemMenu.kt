package com.fde.notepad.menu

import android.view.View.OnClickListener

data class ItemMenu(
    val title: String,
    val hotKey: String = "",
    val onClickListener: OnClickListener = OnClickListener {}
)