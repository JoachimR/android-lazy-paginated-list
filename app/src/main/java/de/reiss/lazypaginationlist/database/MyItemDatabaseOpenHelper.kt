package de.reiss.lazypaginationlist.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyItemDatabaseOpenHelper private constructor(context: Context) : SQLiteOpenHelper(context, "itemsdb", null, 1) {

    companion object {
        private var instance: MyItemDatabaseOpenHelper? = null

        @Synchronized fun getInstance(context: Context): MyItemDatabaseOpenHelper {
            if (instance == null) {
                instance = MyItemDatabaseOpenHelper(context)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE items(id INTEGER PRIMARY KEY, title TEXT, message TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

}
