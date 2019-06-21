package ru.dpav.weather

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

open class NonSwipeViewPager(context: Context, attrs: AttributeSet)
	: ViewPager(context, attrs) {

	var enableSwipe: Boolean = false

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