package com.zacharee1.insomnia.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.widget.Switch
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.util.TimeAdapter
import com.zacharee1.insomnia.util.Utils

class TimesConfigureActivity : AppCompatActivity() {
    private val adapter by lazy { TimeAdapter(this) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_configure)

        setUpActionBar()

        val useInfinite = findViewById<Switch>(R.id.use_infinite)
        useInfinite.isChecked = Utils.useInfinite(this)
        useInfinite.setOnCheckedChangeListener { _, isChecked -> Utils.setUseInfinite(this, isChecked) }

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?)
                    = makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder)
                    = adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
                    = adapter.removeItemAt(viewHolder.adapterPosition)
        })

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        helper.attachToRecyclerView(recycler)
    }

    private fun setUpActionBar() {
        val add = LayoutInflater.from(this).inflate(R.layout.add_button, toolbar, false)
        add.setOnClickListener { adapter.addItem() }
        toolbar.addView(add)

        val reset = LayoutInflater.from(this).inflate(R.layout.reset_button, toolbar, false)
        reset.setOnClickListener { adapter.reset() }
        toolbar.addView(reset)

        setSupportActionBar(toolbar)
    }
}