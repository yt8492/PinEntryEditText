package com.yt8492.pinentryedittext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat

class PinEntryEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    val pinLength: Int
    val space: Float
    val pinBackgroundDrawable: Drawable?
    val pinWidth: Float
    val pinHeight: Float
    private val charPaint = Paint(paint)
    private var charSize: Float = 0f // late init
    private val lineCoords: Array<RectF?>
    private var textWidths = floatArrayOf()
    private val textHeight: Int

    init {
        setBackgroundResource(0)
        isCursorVisible = false
        setTextIsSelectable(false)
        maxLines = DEFAULT_PIN_MAX_LINES

        pinLength = attrs?.getAttributeIntValue(XML_NAMESPACE_ANDROID, XML_ANDROID_MAX_LENGTH, DEFAULT_PIN_LENGTH) ?: DEFAULT_PIN_LENGTH

        val textHeightBounds = Rect()
        paint.getTextBounds("|", 0, 1, textHeightBounds)
        textHeight = (textHeightBounds.top + textHeightBounds.bottom) / 2

        context.obtainStyledAttributes(attrs, R.styleable.PinEntryEditText).also { typedArray ->
            space = typedArray.getDimension(R.styleable.PinEntryEditText_space, 10f)
            pinBackgroundDrawable = typedArray.getDrawable(R.styleable.PinEntryEditText_pinBackgroundDrawable)
            pinWidth = typedArray.getDimension(R.styleable.PinEntryEditText_pinWidth, DEFAULT_PIN_SIZE)
            pinHeight = typedArray.getDimension(R.styleable.PinEntryEditText_pinHeight, DEFAULT_PIN_SIZE)
        }.recycle()

        lineCoords = arrayOfNulls(pinLength)

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s ?: ""
                textWidths = FloatArray(text.length)
                paint.getTextWidths(text, 0, text.length, textWidths)
            }
        })

        setBackgroundResource(0)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        textColors?.let { originalTextColors ->
            charPaint.color = originalTextColors.defaultColor
        }
        val availableWidth = width - ViewCompat.getPaddingStart(this) - ViewCompat.getPaddingEnd(this)
        charSize = (availableWidth - space * (pinLength - 1)) / pinLength
        var startX = ViewCompat.getPaddingStart(this).toFloat()
        val top = paddingTop.toFloat()
        repeat(pinLength) { i ->
            lineCoords[i] = RectF(startX, top, startX + pinWidth, top + pinHeight)
            startX += pinWidth + space
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth: Int = (pinWidth * pinLength + space * pinLength).toInt()
        val measuredHeight: Int = (paddingTop + pinHeight + paddingBottom).toInt()
        setMeasuredDimension(View.resolveSizeAndState(measuredWidth, widthMeasureSpec, 1),
            View.resolveSizeAndState(measuredHeight, heightMeasureSpec, 0))
    }

    override fun onDraw(canvas: Canvas) {
        val text = text ?: ""
        paint.getTextWidths(text, 0, text.length, textWidths)
        repeat(pinLength) { i ->
            lineCoords[i]?.let { lineCoord ->
                pinBackgroundDrawable?.let { pinBackground ->
                    pinBackground.setBounds(lineCoord.left.toInt(), lineCoord.top.toInt(), lineCoord.right.toInt(), lineCoord.bottom.toInt())
                    pinBackground.draw(canvas)
                }
                val middle = lineCoord.left + pinWidth / 2
                if (i < text.length) {
                    canvas.drawText(text, i, i + 1, middle - textWidths[i] / 2, (lineCoord.top + lineCoord.bottom) / 2 - textHeight, charPaint)
                }
            }
        }
    }

    companion object {
        private const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"

        private const val XML_ANDROID_MAX_LENGTH = "maxLength"

        private const val DEFAULT_PIN_SIZE = 48f
        private const val DEFAULT_PIN_LENGTH = 4
        private const val DEFAULT_PIN_MAX_LINES = 1
    }
}