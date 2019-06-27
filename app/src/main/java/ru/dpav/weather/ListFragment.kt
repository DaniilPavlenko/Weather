package ru.dpav.weather

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.fragment_list.view.*
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.ListPresenter
import ru.dpav.weather.views.ListView

class ListFragment : MvpAppCompatFragment(), ListView {

	private lateinit var mAdapter: CitiesAdapter

	@InjectPresenter
	lateinit var mListPresenter: ListPresenter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?): View? {
		val view = inflater
			.inflate(R.layout.fragment_list, container, false)
		val viewManager = LinearLayoutManager(activity!!)
		mAdapter = CitiesAdapter(mListPresenter, mvpDelegate)
		val recyclerView = view.cities_recycler_view
		with(recyclerView) {
			setHasFixedSize(true)
			layoutManager = viewManager
			adapter = mAdapter
		}
		return view
	}

	override fun updateCitiesList(cities: List<City>) {
		view?.citiesNotFound?.visibility =
			if (cities.isEmpty()) View.VISIBLE
			else View.GONE
		mAdapter.setCities(cities)
	}

	override fun toggleDropDownInfo(position: Int, shown: Boolean) {
		if (shown) {
			mAdapter.showDropDownInfo(position)
		} else {
			mAdapter.hideDropDownInfo()
		}
	}

	companion object {
		@JvmStatic
		fun newInstance(): ListFragment = ListFragment()
	}
}