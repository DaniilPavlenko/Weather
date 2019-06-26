package ru.dpav.weather

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout

class ImagesArrayAdapter
@SuppressLint("ResourceType")
constructor(context: Context, private val images: Array<Int>)
	: ArrayAdapter<Int>(context, R.drawable.weather_icon_01, images) {

	override fun getDropDownView(
		position: Int,
		convertView: View?,
		parent: ViewGroup): View {
		return getImageForPosition(position)
	}

	override fun getView(
		position: Int,
		convertView: View?,
		parent: ViewGroup): View {
		return getImageForPosition(position)
	}

	private fun getImageForPosition(position: Int): View {
		val imageView = ImageView(context)
		imageView.setBackgroundResource(images[position])
		val layoutParams = LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.WRAP_CONTENT,
			ViewGroup.LayoutParams.WRAP_CONTENT)
		layoutParams.gravity = Gravity.CENTER
		imageView.layoutParams = layoutParams
		return imageView
	}
}