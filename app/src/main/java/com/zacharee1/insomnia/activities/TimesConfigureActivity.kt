package com.zacharee1.insomnia.activities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.util.TimeAdapter
import com.zacharee1.insomnia.util.WakeState
import com.zacharee1.insomnia.util.dpAsPx
import com.zacharee1.insomnia.util.toBitmap
import java.util.*


class TimesConfigureActivity : AppCompatActivity(), TimeAdapter.DragCallback, TimeAdapter.ItemRemovedCallback {
    private val adapter by lazy { TimeAdapter(this, this, this) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?)
                = makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder)
                = adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
                = adapter.removeItemAt(viewHolder.adapterPosition)

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Get RecyclerView item from the ViewHolder
                val itemView = viewHolder.itemView

                val p = Paint()
                val drawable = ContextCompat.getDrawable(this@TimesConfigureActivity, R.drawable.delete)!!.mutate()
                drawable.setTint(Color.WHITE)
                val icon: Bitmap = drawable.toBitmap()

                if (dX > 0) {
                    p.setARGB(255, 255, 0, 0)

                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), dX,
                            itemView.bottom.toFloat(), p)

                    c.drawBitmap(icon,
                            itemView.left.toFloat() + dpAsPx(16),
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            p)
                } else {
                    p.setARGB(255, 255, 0, 0)

                    c.drawRect(itemView.right.toFloat() + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat(), p)

                    c.drawBitmap(icon,
                            itemView.right.toFloat() - dpAsPx(16) - icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            p)
                }

                val alpha = 1.0f - Math.abs(dX) / viewHolder.itemView.width.toFloat()
                viewHolder.itemView.alpha = alpha
                viewHolder.itemView.translationX = dX

            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_configure)

        setUpActionBar()

        val recycler = findViewById<RecyclerView>(R.id.recycler)

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        helper.attachToRecyclerView(recycler)
    }

    override fun onStartDrag(holder: TimeAdapter.Holder): Boolean {
        helper.startDrag(holder)
        return true
    }

    override fun onItemRemoved(item: WakeState, position: Int) {
        val format = String.format(Locale.getDefault(), resources.getString(R.string.time_removed_format), (item.time / 1000))
        val snackbar = Snackbar.make(findViewById<ConstraintLayout>(R.id.root), format, Snackbar.LENGTH_LONG)
        snackbar.setAction(R.string.undo) { adapter.addItemAt(item, position) }
        snackbar.show()
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