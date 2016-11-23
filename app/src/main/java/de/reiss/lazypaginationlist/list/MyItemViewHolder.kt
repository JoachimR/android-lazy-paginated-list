package de.reiss.lazypaginationlist.list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import de.reiss.lazypaginationlist.R

class MyItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    protected var myItem: MyItem? = null

    val loadedRoot: View
    val loadingRoot: View

    val title: TextView
    val message: TextView

    init {
        loadedRoot = view.findViewById(R.id.list_item_loaded_root)
        loadingRoot = view.findViewById(R.id.list_item_loading_root)
        title = view.findViewById(R.id.list_item_title) as TextView
        message = view.findViewById(R.id.list_item_message) as TextView

        title.setOnClickListener({
            println("clicked title")
        })
    }

    fun bindItem(myItem: MyItem?) {
        this.myItem = myItem

        val item = myItem
        if (item == null) {
            loadedRoot.visibility = View.GONE
            loadingRoot.visibility = View.VISIBLE
        } else {
            loadedRoot.visibility = View.VISIBLE
            loadingRoot.visibility = View.GONE
            refreshViews(item)
        }
    }

    fun refreshViews(myItem: MyItem) {
        title.text = myItem.title
        message.text = myItem.message
    }

}
