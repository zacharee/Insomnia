package com.zacharee1.insomnia.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zacharee1.insomnia.App
import com.zacharee1.insomnia.R
import java.util.*

class TimeAdapter(private val context: Context, private val dragCallback: DragCallback, private val itemRemovedCallback: ItemRemovedCallback) : androidx.recyclerview.widget.RecyclerView.Adapter<TimeAdapter.Holder>() {
    private val states = ArrayList(context.getSavedTimes())
    private val app = App.get(context)
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun getItemCount() = states.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.time_configure_item, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val state = states[position]

        holder.timeSelectedListener = object : TimeAdapterListener {
            override fun newTime(time: Long) {
                state.time = time
                holder.setTime(time)
                context.saveTimes(states)
            }
        }

        holder.setClickListener { app.setToState(states[holder.bindingAdapterPosition]) }
        holder.setDragListener { _, _ -> dragCallback.onStartDrag(holder) }

        holder.setTime(state.time)
    }

    fun moveItem(from: Int, to: Int) {
        mainHandler.post {
            val state = states.removeAt(from)

            states.add(to, state)
            notifyItemMoved(from, to)
            context.saveTimes(states)
        }
    }

    fun removeItemAt(index: Int) {
        mainHandler.post {
            val item = states.removeAt(index)
            notifyItemRemoved(index)
            context.saveTimes(states)

            itemRemovedCallback.onItemRemoved(item, index)
        }
    }

    fun addItem() {
        Dialog(context, object : TimeAdapterListener {
            override fun newTime(time: Long) {
                mainHandler.post {
                    states.add(WakeState(time))
                    context.saveTimes(states)
                    notifyItemInserted(states.lastIndex)
                }
            }
        }).show()
    }

    fun addItemAt(state: WakeState, position: Int) {
        mainHandler.post {
            states.add(position, state)
            context.saveTimes(states)
            notifyItemInserted(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reset() {
        mainHandler.post {
            states.clear()
            states.addAll(App.DEFAULT_STATES)
            context.saveTimes(states)
            notifyDataSetChanged()
        }
    }

    class Holder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        private val labelView: TextView = view.findViewById<TextView>(R.id.time_label)

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

    class Dialog(context: Context, private val timeSelectedListener: TimeAdapterListener?) : MaterialAlertDialogBuilder(context) {
        private val view = View.inflate(context, R.layout.time_edit_dialog, null)
        private val textBox = view.findViewById<EditText>(R.id.time_edit)

        init {
            setTitle(context.resources.getString(R.string.time))
            setView(view)
        }

        override fun create(): androidx.appcompat.app.AlertDialog {
            return super.create().apply {
                setButton(AlertDialog.BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
                    val time = textBox.text.toString().toLong()
                    timeSelectedListener?.newTime(if (time < 0) time else time * 1000)
                }
                setButton(AlertDialog.BUTTON_NEGATIVE, context.resources.getText(android.R.string.cancel)) { _, _ ->
                    cancel()
                }
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