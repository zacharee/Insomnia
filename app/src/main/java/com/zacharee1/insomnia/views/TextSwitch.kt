package com.zacharee1.insomnia.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.zacharee1.insomnia.R
import com.zacharee1.insomnia.databinding.TextSwitchBinding

class TextSwitch(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet) {
    private val binding = TextSwitchBinding.inflate(LayoutInflater.from(context), this, true)

    var isChecked: Boolean
        get() = binding.switch1.isChecked
        set(value) { binding.switch1.isChecked = value }

    var onCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
        set(value) { binding.switch1.setOnCheckedChangeListener(value) }

    var titleText: CharSequence?
        get() = binding.title.text
        set(value) { binding.title.text = value }

    var summaryText: CharSequence?
        get() = binding.summary.text
        set(value) {
            binding.summary.text = value
            binding.summary.isVisible = !value.isNullOrEmpty()
        }

    init {
        binding.root.setOnClickListener {
            isChecked = !isChecked
        }

        val array = context.theme.obtainStyledAttributes(attributeSet, R.styleable.TextSwitch, 0, 0)

        try {
            val titleSize = array.getDimensionPixelSize(R.styleable.TextSwitch_title_text_size, 0)
            if (titleSize != 0) binding.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())

            val summarySize = array.getDimensionPixelSize(R.styleable.TextSwitch_summary_text_size, 0)
            if (summarySize != 0) binding.summary.setTextSize(TypedValue.COMPLEX_UNIT_PX, summarySize.toFloat())

            titleText = array.getText(R.styleable.TextSwitch_title_text)
            summaryText = array.getText(R.styleable.TextSwitch_summary_text)
        } finally {
            array.recycle()
        }
    }
}