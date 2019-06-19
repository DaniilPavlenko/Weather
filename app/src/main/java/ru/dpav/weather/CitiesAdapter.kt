package ru.dpav.weather

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.arellomobile.mvp.MvpDelegate
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.ListPresenter

class CitiesAdapter(
	var mParentPresenter: ListPresenter,
	var mMvpDelegate: MvpDelegate<*>,
	private var recyclerView: RecyclerView,
	private var cities: ArrayList<City>)
	: RecyclerView.Adapter<CityHolder>() {

	private var openedPosition = -1

	override fun onCreateViewHolder(parent: ViewGroup, index: Int): CityHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_city, parent, false)
		return CityHolder(view, mMvpDelegate, mParentPresenter)
	}

	override fun getItemCount(): Int {
		return cities.count()
	}

	override fun onBindViewHolder(holder: CityHolder, index: Int) {
		val city = cities[index]
		holder.bind(city, index == openedPosition)
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