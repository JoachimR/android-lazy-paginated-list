package de.reiss.lazypaginationlist.list

import de.reiss.lazypaginationlist.generic.LazyList
import java.io.Closeable

class MyItemPaginatedList(private var lazyList: LazyList<MyItem>, private var itemsPerPage: Int) : Iterable<MyItem>, Closeable {

    private var currentPage: Int = 0
    private var pageCount: Int = 0

    init {
        pageCount = (lazyList.size - 1) / itemsPerPage + 1
    }

    val currentStartIndex: Int
        get() = currentPage * itemsPerPage

    val currentEndIndex: Int
        get() = Math.max(currentStartIndex + currentPageItemCount - 1, 0)

    fun setCurrentPage(page: Int) {
        if (page < 0) {
            this.currentPage = 0
        } else if (page >= pageCount) {
            this.currentPage = pageCount - 1
        } else {
            this.currentPage = page
        }
    }

    fun getSubListPosition(position: Int): Int {
        return position - currentStartIndex
    }

    fun getFullListPosition(position: Int): Int {
        return currentStartIndex + position
    }

    fun get(position: Int): MyItem? {
        return lazyList.get(getFullListPosition(position))
    }

    fun goToNextPage() {
        setCurrentPage(currentPage + 1)
    }

    fun goToPriorPage() {
        setCurrentPage(currentPage - 1)
    }

    val fullListSize: Int
        get() = lazyList.size

    val currentPageItemCount: Int
        get() = Math.min(itemsPerPage, lazyList.size - currentPage * itemsPerPage)

    val isPaginationNecessary: Boolean
        get() = pageCount > 1

    val isOnFirstPage: Boolean
        get() = currentPage == 0

    val isOnLastPage: Boolean
        get() = currentPage == pageCount - 1

    override fun iterator(): Iterator<MyItem> {
        return lazyList.iterator()
    }

    override fun close() {
        lazyList.close()
    }

}