package ru.dpav.weather

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
	private lateinit var mRecyclerView: RecyclerView
	private var mCities: ArrayList<City> = ArrayList()

	@InjectPresenter
	lateinit var mListPresenter: ListPresenter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_list, container, false)
		mRecyclerView = view.cities_recycler_view
		val viewManager = LinearLayoutManager(activity!!)
		val viewAdapter = CitiesAdapter(mListPresenter, mvpDelegate, mRecyclerView, mCities)
		with(mRecyclerView) {
			setHasFixedSize(true)
			layoutManager = viewManager
			adapter = viewAdapter
		}
		return view
	}

	override fun updateCitiesList(cities: List<City>) {
		mCities = cities as ArrayList<City>
		(mRecyclerView.adapter as CitiesAdapter).setCities(cities)
	}

	override fun showDropDownInfo(position: Int) {
		(mRecyclerView.adapter as CitiesAdapter).showDropDownInfo(position)
	}

	override fun hideDropDownInfo() {
		(mRecyclerView.adapter as CitiesAdapter).hideDropDownInfo()
	}

	companion object {
		@JvmStatic
		fun newInstance(): ListFragment = ListFragment()
	}
}