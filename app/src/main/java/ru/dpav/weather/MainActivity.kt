package ru.dpav.weather

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
		view_pager.adapter = sectionsPagerAdapter
		tabs.setupWithViewPager(view_pager)
	}
}