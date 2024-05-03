package ru.dpav.weather

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

open class NonSwipeViewPager(
    context: Context,
    attrs: AttributeSet,
) : ViewPager(context, attrs) {

    private var enableSwipe: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.enableSwipe) {
            super.onTouchEvent(event)
        } else false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.enableSwipe) {
            super.onInterceptTouchEvent(event)
        } else false

    }
}
