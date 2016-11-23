package de.reiss.lazypaginationlist.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import de.reiss.lazypaginationlist.generic.DatabaseInterface
import de.reiss.lazypaginationlist.list.MyItem

class MyItemDatabase(context: Context) : DatabaseInterface<MyItem> {

    private var dbHelper: MyItemDatabaseOpenHelper

    init {
        dbHelper = MyItemDatabaseOpenHelper.getInstance(context)
    }

    override fun getCursor(orderDesc: Boolean): Cursor {
        val query = "SELECT * FROM items ORDER BY id ".plus(
                if (orderDesc) {
                    "DESC"
                } else {
                    "ASC"
                })
        return dbHelper.readableDatabase.rawQuery(query, null)
    }

    override fun insert(items: List<MyItem>) {
        dbHelper.writableDatabase.beginTransaction();
        try {
            items.forEach {
                dbHelper.writableDatabase.insert("items", null, contentValues(it))
            }
            dbHelper.writableDatabase.setTransactionSuccessful();
        } finally {
            dbHelper.writableDatabase.endTransaction();
        }
    }

    override fun clear() {
        dbHelper.writableDatabase.delete("items", null, null)
    }

    private fun contentValues(item: MyItem): ContentValues {
        val values = ContentValues()
        values.put("title", item.title)
        values.put("message", item.message)
        return values
    }

}

