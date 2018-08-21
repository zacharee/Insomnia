package com.zacharee1.insomnia.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.zacharee1.insomnia.App
import com.zacharee1.insomnia.R
import java.util.*

class TimeAdapter(private val context: Context, private val dragCallback: DragCallback, private val itemRemovedCallback: ItemRemovedCallback) : RecyclerView.Adapter<TimeAdapter.Holder>() {
    private val states = ArrayList(context.getSavedTimes())
    private val app = App.get(context)

    override fun getItemCount() = states.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.time_configure_item, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val state = states[position]

        holder.timeSelectedListener = object : TimeAdapterListener {
            override fun newTime(time: Long) {
                state.time = time
                holder.setTime(time)
                context.saveTimes(states)
            }
        }

        holder.setClickListener(View.OnClickListener { app.setToState(states[holder.adapterPosition]) })
        holder.setDragListener(View.OnTouchListener { _, _ -> dragCallback.onStartDrag(holder) })

        holder.setTime(state.time)
    }

    fun moveItem(from: Int, to: Int): Boolean {
        val state = states.removeAt(from)

        states.add(to, state)
        notifyItemMoved(from, to)
        context.saveTimes(states)

        return true
    }

    fun removeItemAt(index: Int) {
        val item = states.removeAt(index)
        notifyItemRemoved(index)
        context.saveTimes(states)

        itemRemovedCallback.onItemRemoved(item, index)
    }

    fun addItem() {
        Dialog(context, object : TimeAdapterListener {
            override fun newTime(time: Long) {
                val title = if (time < 0) R.string.time_infinite else R.string.custom
                states.add(WakeState(title, R.drawable.on, time))
                notifyItemInserted(states.lastIndex)
            }
        }).show()
    }

    fun addItemAt(state: WakeState, position: Int) {
        states.add(position, state)
        notifyItemInserted(position)
    }

    fun reset() {
        states.clear()
        states.addAll(App.DEFAULT_STATES)
        context.saveTimes(states)
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val labelView = view.findViewById<TextView>(R.id.time_label)

        var timeSelectedListener: TimeAdapterListener? = null

        init {
            view.findViewById<ImageView>(R.id.time_config).setOnClickListener { showDialog() }
        }

        private fun showDialog() {
            val dialog = Dialog(itemView.context, timeSelectedListener)
            dialog.show()
        }

        fun setTime(time: Long) {
            val t: Any = if (time < 0) itemView.context.resources.getString(R.string.time_infinite) else (time / 1000)
            val format = String.format(Locale.getDefault(),
                    labelView.context.resources.getString(R.string.time_label_format), t)
            labelView.text = format
        }

        fun setClickListener(listener: View.OnClickListener) {
            itemView.setOnClickListener(listener)
        }

        @SuppressLint("ClickableViewAccessibility")
        fun setDragListener(listener: View.OnTouchListener) {
            itemView.findViewById<ImageView>(R.id.handle).setOnTouchListener(listener)
        }
    }

    class Dialog(context: Context, private val timeSelectedListener: TimeAdapterListener?) : AlertDialog(context) {
        private val view = View.inflate(context, R.layout.time_edit_dialog, null)
        private val textBox = view.findViewById<EditText>(R.id.time_edit)

        init {
            setTitle(context.resources.getString(R.string.time))
            setView(view)

            setButton(AlertDialog.BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
                val time = textBox.text.toString().toLong()
                timeSelectedListener?.newTime(if (time < 0) time else time * 1000)
            }
            setButton(AlertDialog.BUTTON_NEGATIVE, context.resources.getText(android.R.string.cancel)) { _, _ ->
                cancel()
            }
        }
    }

    interface DragCallback {
        fun onStartDrag(holder: Holder): Boolean
    }

    interface ItemRemovedCallback {
        fun onItemRemoved(item: WakeState, position: Int)
    }
}