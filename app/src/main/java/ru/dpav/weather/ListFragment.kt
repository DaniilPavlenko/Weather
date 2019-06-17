package ru.dpav.weather

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpDelegate
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.ListPresenter
import ru.dpav.weather.views.ListView

class ListFragment : MvpAppCompatFragment(), ListView {
	private lateinit var mRecyclerView: RecyclerView
	private var mCities: ArrayList<City> = ArrayList()

	@InjectPresenter(type = PresenterType.GLOBAL, tag = TAG_PRESENTER)
	lateinit var mListPresenter: ListPresenter

	@ProvidePresenter(type = PresenterType.GLOBAL, tag = TAG_PRESENTER)
	fun provideAccountPresenter(): ListPresenter {
		return ListPresenter()
	}

	override fun onCreateView(inflater: LayoutInflater,
		container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_list, container, false)
		mRecyclerView = view.findViewById(R.id.cities_recycler_view)
		val viewManager = LinearLayoutManager(activity!!)
		val viewAdapter = CitiesAdapter(mvpDelegate, mRecyclerView, mCities)
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

	private class CitiesAdapter(var mMvpDelegate: MvpDelegate<*>, private var recyclerView: RecyclerView,
		private var cities: ArrayList<City>) : RecyclerView.Adapter<CityHolder>() {

		private var openedPosition = -1

		override fun onCreateViewHolder(parent: ViewGroup, index: Int): CityHolder {
			val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_city, parent, false)
			return CityHolder(view, mMvpDelegate)
		}

		override fun getItemCount(): Int {
			return cities.count()
		}

		override fun onBindViewHolder(holder: CityHolder, index: Int) {
			val city = cities[index]
			holder.bind(city, index, index == openedPosition)
		}

		fun hideDropDownInfo() {
			openedPosition = -1
		}

		fun showDropDownInfo(position: Int) {
			this.notifyItemChanged(openedPosition)
			openedPosition = position
			recyclerView.smoothScrollToPosition(position + 1)
		}

		fun setCities(newCities: List<City>) {
			cities.clear()
			cities.addAll(newCities)
			notifyDataSetChanged()
			hideDropDownInfo()
			recyclerView.smoothScrollToPosition(0)
		}
	}

	override fun showDropDownInfo(position: Int) {
		(mRecyclerView.adapter as CitiesAdapter).showDropDownInfo(position)
	}

	override fun hideDropDownInfo() {
		(mRecyclerView.adapter as CitiesAdapter).hideDropDownInfo()
	}

	companion object {
		const val TAG_PRESENTER: String = "listPresenter"

		@JvmStatic
		fun newInstance(): ListFragment = ListFragment()
	}
}