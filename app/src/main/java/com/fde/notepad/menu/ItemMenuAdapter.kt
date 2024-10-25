package com.fde.notepad.menu

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fde.notepad.R

class ItemMenuAdapter(private val itemMenuList: List<ItemMenu>) :
    RecyclerView.Adapter<ItemMenuAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), OnClickListener {
        val titleTxt: TextView = view.findViewById(R.id.itemMenuTitleTxt)
        val hotKeyTxt: TextView = view.findViewById(R.id.hotKeyTxt)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            itemMenuList[adapterPosition].onClickListener.onClick(view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_menu_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTxt.text = itemMenuList[position].title
        holder.hotKeyTxt.text = itemMenuList[position].hotKey
    }

    override fun getItemCount() = itemMenuList.size
}
