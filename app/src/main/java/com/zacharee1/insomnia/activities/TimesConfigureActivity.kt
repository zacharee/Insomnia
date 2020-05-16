package com.zacharee1.insomnia.activities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.widget.CompoundButton
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.util.*
import com.zacharee1.insomnia.views.TextSwitch
import java.util.*


class TimesConfigureActivity : AppCompatActivity(), TimeAdapter.DragCallback, TimeAdapter.ItemRemovedCallback {
    private val adapter by lazy { TimeAdapter(this, this, this) }
    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?)
//                = makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
//                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

        override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int)
                = adapter.removeItemAt(viewHolder.adapterPosition)

        override fun onChildDraw(c: Canvas, recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
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
        setUpListeners()

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler)

        recycler.adapter = adapter
        recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL))

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

    private fun setUpListeners() {
        val plugged = findViewById<TextSwitch>(R.id.turn_on_plugged)
        plugged.isChecked = activateWhenPlugged()
        plugged.onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _, c -> setActivateWhenPlugged(c) }
    }
}