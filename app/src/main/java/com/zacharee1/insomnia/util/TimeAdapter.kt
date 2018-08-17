package com.zacharee1.insomnia.util

import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.tiles.CycleTile
import java.util.*

class TimeAdapter(private val context: Context) : RecyclerView.Adapter<TimeAdapter.Holder>() {
    private val states = ArrayList(Utils.getSavedTimes(context))

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
                Utils.saveTimes(context, states)
            }
        }

        holder.setTime(state.time)
    }

    fun moveItem(from: Int, to: Int): Boolean {
        val state = states.removeAt(from)

        states.add(to, state)
        notifyItemMoved(from, to)
        Utils.saveTimes(context, states)

        return true
    }

    fun removeItemAt(index: Int) {
        states.removeAt(index)
        notifyItemRemoved(index)
        Utils.saveTimes(context, states)
    }

    fun addItem() {
        Dialog(context, object : TimeAdapterListener {
            override fun newTime(time: Long) {
                states.add(CycleTile.WakeState(R.string.custom, R.drawable.on, time))
                notifyItemInserted(states.lastIndex)
            }
        }).show()
    }

    fun reset() {
        states.clear()
        states.addAll(CycleTile.DEFAULT_STATES)
        Utils.saveTimes(context, states)
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val labelView = view.findViewById<TextView>(R.id.time_label)

        var timeSelectedListener: TimeAdapterListener? = null

        init {
            view.setOnClickListener { showDialog() }
        }

        private fun showDialog() {
            val dialog = Dialog(itemView.context, timeSelectedListener)
            dialog.show()
        }

        fun setTime(time: Long) {
            val format = String.format(Locale.getDefault(),
                    labelView.context.resources.getString(R.string.time_label_format),
                    (time / 1000))
            labelView.text = format
        }
    }

    class Dialog(context: Context, private val timeSelectedListener: TimeAdapterListener?) : AlertDialog(context) {
        private val view = View.inflate(context, R.layout.time_edit_dialog, null)
        private val textBox = view.findViewById<EditText>(R.id.time_edit)

        init {
            setTitle(context.resources.getString(R.string.time))
            setView(view)

            setButton(AlertDialog.BUTTON_POSITIVE, context.resources.getText(android.R.string.ok)) { _, _ ->
                timeSelectedListener?.newTime(textBox.text.toString().toLong() * 1000)
            }
            setButton(AlertDialog.BUTTON_NEGATIVE, context.resources.getText(android.R.string.cancel)) { _, _ ->
                cancel()
            }
        }
    }
}