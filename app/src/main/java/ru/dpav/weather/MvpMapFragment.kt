package ru.dpav.weather

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.arellomobile.mvp.MvpDelegate
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment

abstract class MvpMapFragment : Fragment() {

	abstract val mapContainerId: Int
	open fun onMapReady() {}

	private var isMapReady = false
	private var mIsStateSaved: Boolean = false
	lateinit var map: GoogleMap
	val mapView: View
		get() = (childFragmentManager.findFragmentById(mapContainerId) as SupportMapFragment).view!!
	private val mvpDelegate: MvpDelegate<out MvpMapFragment> by lazy { MvpDelegate(this) }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		var mapFragment = childFragmentManager.findFragmentById(mapContainerId) as? SupportMapFragment
		if (mapFragment == null) {
			mapFragment = SupportMapFragment()
			childFragmentManager.beginTransaction().replace(mapContainerId, mapFragment).commit()
		}
		isMapReady = false
		mapFragment.getMapAsync {
			this.map = it
			isMapReady = true
			onMapReady()
			mvpDelegate.onAttach()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		mvpDelegate.onCreate(savedInstanceState)
	}

	override fun onStart() {
		super.onStart()
		mIsStateSaved = false
		if (isMapReady) {
			mvpDelegate.onAttach()
		}
	}

	override fun onResume() {
		super.onResume()
		mIsStateSaved = false
		if (isMapReady) {
			mvpDelegate.onAttach()
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		mIsStateSaved = true
		mvpDelegate.onSaveInstanceState(outState)
		mvpDelegate.onDetach()
	}

	override fun onStop() {
		super.onStop()
		mvpDelegate.onDetach()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		mvpDelegate.onDetach()
		mvpDelegate.onDestroyView()
		if (isMapReady) {
			map.clear()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		if (activity!!.isFinishing) {
			mvpDelegate.onDestroy()
			return
		}
		if (mIsStateSaved) {
			mIsStateSaved = false
			return
		}
		var anyParentIsRemoving = false
		var parent = parentFragment
		while (!anyParentIsRemoving && parent != null) {
			anyParentIsRemoving = parent.isRemoving
			parent = parent.parentFragment
		}
		if (isRemoving || anyParentIsRemoving) {
			mvpDelegate.onDestroy()
		}
	}
}