package com.wenshu.app.ui.home

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import kotlin.math.abs

class TopBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onSwipeDown: (() -> Unit)? = null

    private var startY = 0f
    private var startX = 0f
    private var tracking = false
    private var triggered = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.rawY
                startX = ev.rawX
                tracking = true
                triggered = false
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (tracking && !triggered) {
                    val dy = ev.rawY - startY
                    val dx = ev.rawX - startX
                    if (dy > 60 && dy > abs(dx) * 1.5f) {
                        triggered = true
                        onSwipeDown?.invoke()
                        return true
                    }
                }
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                tracking = false
                triggered = false
                return false
            }
        }
        return false
    }
}
