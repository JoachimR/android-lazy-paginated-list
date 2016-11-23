package de.reiss.lazypaginationlist.list

import android.database.Cursor
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.reiss.lazypaginationlist.R
import de.reiss.lazypaginationlist.generic.LazyList
import java.io.Closeable

class PaginatedCursorAdapter constructor(val activity: FragmentActivity,
                                         cursor: Cursor,
                                         val itemsPerPage: Int) : RecyclerView.Adapter<MyItemViewHolder>(), Closeable {

    var paginatedList: MyItemPaginatedList

    init {
        setHasStableIds(true)
        paginatedList = createPaginatedList(cursor)
    }

    fun paginate(forward: Boolean) {
        if (forward) {
            paginatedList.goToNextPage()
        } else {
            paginatedList.goToPriorPage()
        }

        notifyDataSetChanged()
    }

    fun refreshCursor(cursor: Cursor) {
        paginatedList = createPaginatedList(cursor)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return paginatedList.getFullListPosition(position).toLong()
    }

    override fun getItemCount(): Int {
        return paginatedList.currentPageItemCount
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyItemViewHolder {
        when (viewType) {
            R.layout.list_item -> {
                return MyItemViewHolder(LayoutInflater.from(activity)
                        .inflate(R.layout.list_item, viewGroup, false))
            }
            else -> {
                throw IllegalArgumentException("unknown viewtype $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
        val podcast = paginatedList.get(position)
        holder.bindItem(podcast)
    }

    override fun close() {
        paginatedList.close()
    }

    private fun createPaginatedList(cursor: Cursor): MyItemPaginatedList {
        return MyItemPaginatedList(
                LazyList<MyItem>(cursor,
                        LazyList.Converter { cursor ->
                            MyItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2))
                        }, true),
                itemsPerPage)
    }

}