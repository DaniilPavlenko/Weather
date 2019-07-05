package ru.dpav.weather

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import com.arellomobile.mvp.MvpAppCompatActivity
import kotlinx.android.synthetic.main.activity_choose_icon.*
import ru.dpav.weather.util.Util

class ChooseIconActivity : MvpAppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		setContentView(R.layout.activity_choose_icon)

		val iconsBox = iconsContainer
		val childCount = iconsBox.childCount
		for (i in 0 until childCount) {
			val childView = iconsBox.getChildAt(i)
			if (childView is ImageButton) {
				childView.setOnClickListener {
					val drawableName = childView.tag.toString()
					val iconRes = Util.Icons
						.getDrawableIdByName(this, drawableName)
					returnResult(iconRes)
				}
			}
		}
		closeIconDialogButton.setOnClickListener{
			finish()
		}
	}

	private fun returnResult(iconRes: Int) {
		val intent = Intent().putExtra(SELECTED_ICON, iconRes)
		setResult(Activity.RESULT_OK, intent)
		finish()
	}

	companion object {
		const val SELECTED_ICON = "selectedIcon"

		fun newIntent(context: Context): Intent =
			Intent(context, ChooseIconActivity::class.java)
	}
}