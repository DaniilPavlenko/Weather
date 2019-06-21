package ru.dpav.weather

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
	: FragmentPagerAdapter(fm) {
	override fun getItem(position: Int): Fragment =
		when (TAB_TITLES[position]) {
			R.string.map -> MapFragment.newInstance()
			R.string.list -> ListFragment.newInstance()
			else -> MapFragment.newInstance()
		}

	override fun getPageTitle(position: Int): CharSequence? =
		context.resources.getString(TAB_TITLES[position])

	override fun getCount(): Int = TAB_TITLES.size

	companion object {
		private val TAB_TITLES = arrayOf(
			R.string.map,
			R.string.list
		)
	}
}