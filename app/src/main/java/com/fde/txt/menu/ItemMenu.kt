package com.fde.txt.menu

import android.view.View.OnClickListener

data class ItemMenu(
    val title: String,
    val hotKey: String = "",
    val onClickListener: OnClickListener = OnClickListener {}
)