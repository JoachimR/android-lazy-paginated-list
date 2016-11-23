package de.reiss.lazypaginationlist

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import de.reiss.lazypaginationlist.database.MyItemDatabase
import de.reiss.lazypaginationlist.list.MyItem
import de.reiss.lazypaginationlist.list.PaginatedCursorAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: MyItemDatabase
    private lateinit var adapter: PaginatedCursorAdapter

    private val itemsPerPage = 100

    private var orderDesc: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)

        database = MyItemDatabase(this)
        database.clear()

        setupViews()

        adapter = PaginatedCursorAdapter(this, database.getCursor(orderDesc), itemsPerPage)
        activity_main_list.adapter = adapter
    }

    private fun setupViews() {
        activity_main_list.setHasFixedSize(true)
        activity_main_list.layoutManager = LinearLayoutManager(this)

        activity_main_paginate_backward.setOnClickListener {
            adapter.paginate(false)
            updatePagination()
        }
        activity_main_paginate_forward.setOnClickListener {
            adapter.paginate(true)
            updatePagination()
        }
    }

    override fun onStart() {
        super.onStart()
        refreshContent()
    }

    override fun onDestroy() {
        adapter.close()
        super.onDestroy()
    }

    fun refreshContent() {
        val cursor = database.getCursor(orderDesc)
        if (cursor.count == 0) {
            insertFirstTime()
        } else {
            adapter.refreshCursor(cursor)
        }
        updatePagination()
    }

    private fun updatePagination() {
        if (adapter.paginatedList.isPaginationNecessary) {
            activity_main_pagination_root.visibility = View.VISIBLE
            refreshLeftPaginate()
            refreshRightPaginate()
            activity_main_pagination_text.text = getString(R.string.pagination_header,
                    (adapter.paginatedList.currentStartIndex + 1).toString(),
                    (adapter.paginatedList.currentEndIndex + 1).toString(),
                    adapter.paginatedList.fullListSize.toString())
        } else {
            activity_main_pagination_root.visibility = View.GONE
        }
    }

    private fun refreshLeftPaginate() {
        if (!adapter.paginatedList.isOnFirstPage) {
            activity_main_paginate_backward.setEnabled(true)
            activity_main_paginate_backward.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        } else {
            activity_main_paginate_backward.setEnabled(false)
            activity_main_paginate_backward.setColorFilter(ContextCompat.getColor(this, R.color.grey))
        }
    }

    private fun refreshRightPaginate() {
        if (!adapter.paginatedList.isOnLastPage) {
            activity_main_paginate_forward.setEnabled(true)
            activity_main_paginate_forward.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        } else {
            activity_main_paginate_forward.setEnabled(false)
            activity_main_paginate_forward.setColorFilter(ContextCompat.getColor(this, R.color.grey))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_toggle_sort)?.icon =
                ContextCompat.getDrawable(this,
                        if (orderDesc) {
                            R.drawable.ic_arrow_upward_white_24dp
                        } else {
                            R.drawable.ic_arrow_downward_white_24dp
                        })
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == R.id.action_toggle_sort) {
            orderDesc = !orderDesc
            refreshContent()
            invalidateOptionsMenu()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertFirstTime() {
        object : AsyncTask<Void, Int, Boolean>() {

            private lateinit var dialog: ProgressDialog

            private val amount = 1000

            override fun onPreExecute() {
                super.onPreExecute()
                dialog = ProgressDialog(this@MainActivity)
                dialog.setMessage("Filling sample database on first launch...")
                dialog.show()
            }

            override fun onPostExecute(result: Boolean) {
                dialog.dismiss()
                refreshContent()
            }

            override fun doInBackground(vararg p0: Void?): Boolean {
                val items = ArrayList<MyItem>()
                for (i in 0..amount - 1) {
                    items.add(MyItem(-1, "Title $i", "message $i message $i  message $i " +
                            " message $i  message $i message $i message $i  " +
                            "message $i  message $i  message $i"))
                }

                database.insert(items)
                return true
            }

        }.execute()

    }

}
