package de.reiss.lazypaginationlist.generic

import android.database.Cursor

interface DatabaseInterface<T> {

    fun getCursor(orderDesc : Boolean): Cursor

    fun insert(items: List<T>)

    fun clear( )

}
