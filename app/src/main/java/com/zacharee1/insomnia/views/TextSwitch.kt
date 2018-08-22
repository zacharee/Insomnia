package com.zacharee1.insomnia.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import com.zacharee1.insomnia.R

class TextSwitch(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {
    val title: TextView
    val summary: TextView
    val switch: Switch

    var isChecked: Boolean
        get() = switch.isChecked
        set(value) { switch.isChecked = value }

    var onCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
        set(value) { switch.setOnCheckedChangeListener(value) }

    var titleText: CharSequence?
        get() = title.text
        set(value) { title.text = value }

    var summaryText: CharSequence?
        get() = summary.text
        set(value) {
            summary.text = value
            if (value.isNullOrEmpty()) summary.visibility = View.GONE
            else summary.visibility = View.VISIBLE
        }

    init {
        View.inflate(context, R.layout.text_switch, this)

        title = findViewById(R.id.title)
        summary = findViewById(R.id.summary)
        switch = findViewById(R.id.switch1)

        val array = context.theme.obtainStyledAttributes(attributeSet, R.styleable.TextSwitch, 0, 0)

        try {
            val titleSize = array.getDimensionPixelSize(R.styleable.TextSwitch_title_text_size, 0)
            if (titleSize != 0) title.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())

            val summarySize = array.getDimensionPixelSize(R.styleable.TextSwitch_summary_text_size, 0)
            if (summarySize != 0) summary.setTextSize(TypedValue.COMPLEX_UNIT_PX, summarySize.toFloat())

            titleText = array.getText(R.styleable.TextSwitch_title_text)
            summaryText = array.getText(R.styleable.TextSwitch_summary_text)
        } finally {
            array.recycle()
        }
    }
}