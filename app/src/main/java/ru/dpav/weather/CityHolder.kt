package ru.dpav.weather

import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.MvpDelegate
import com.arellomobile.mvp.MvpFacade
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.ListPresenter
import ru.dpav.weather.util.Util
import ru.dpav.weather.views.ListView

class CityHolder(itemView: View, parentDelegate: MvpDelegate<*>)
	: MvpViewHolder(parentDelegate, itemView), ListView {
	private var mCity: City? = null
	private var mTitle: TextView = itemView.findViewById(R.id.city_detail_title)
	private var mTemperature: TextView = itemView.findViewById(R.id.city_detail_temperature)
	private var mWind: TextView = itemView.findViewById(R.id.city_detail_wind)
	private var mCloudy: TextView = itemView.findViewById(R.id.city_detail_cloudy)
	private var mPressure: TextView = itemView.findViewById(R.id.city_detail_pressure)
	private var mDetailInfo: ConstraintLayout = itemView.findViewById(R.id.city_detail)

	@InjectPresenter
	lateinit var mListPresenter: ListPresenter

	@ProvidePresenter
	fun provideAccountPresenter(): ListPresenter {
		MvpFacade.getInstance()
		return ListPresenter(mCity!!.id)
	}

	fun bind(city: City, position: Int, isOpened: Boolean) {
		destroyMvpDelegate()
		mCity = city
		createMvpDelegate()
		mTitle.text = city.name
		mTitle.setOnClickListener { onTitleClick(position) }
		val context = mTitle.context
		mTemperature.text = context.getString(R.string.detail_temperature, city.main!!.temp.toInt())
		val icon = Util.getWeatherIconByName(city.weather?.get(0)!!.icon)
		mTemperature.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
		mWind.text = context.getString(R.string.detail_wind, city.wind!!.speed.toInt())
		mCloudy.text = context.getString(R.string.detail_cloudy, city.clouds!!.all)
		mPressure.text = context.getString(R.string.detail_pressure, Util.getPressureInMmHg(city.main.pressure))
		mDetailInfo.visibility = if (isOpened) View.VISIBLE else View.GONE
		mDetailInfo.setOnClickListener {
			val cityDetailFragment = CityDetailFragment.newInstance(city)
			cityDetailFragment.show((context as AppCompatActivity).supportFragmentManager, "dialog_city2")
		}
	}

	private fun isDetailVisible(): Boolean = mDetailInfo.visibility == View.VISIBLE

	override fun getMvpChildId(): String? {
		return if (mCity == null) null else mCity!!.id.toString()
	}

	private fun onTitleClick(position: Int) {
		if (!isDetailVisible()) {
			mListPresenter.onShowDropDownInfo(position)
			(MvpFacade.getInstance().presenterStore.get(ListFragment.TAG_PRESENTER) as ListPresenter)
				.onShowDropDownInfo(position)
		} else {
			mListPresenter.onHideDropDownInfo()
			(MvpFacade.getInstance().presenterStore.get(ListFragment.TAG_PRESENTER) as ListPresenter)
				.onHideDropDownInfo()
		}
	}

	override fun showDropDownInfo(position: Int) {
		mDetailInfo.visibility = View.VISIBLE
	}

	override fun hideDropDownInfo() {
		mDetailInfo.visibility = View.GONE
	}

	override fun updateCitiesList(cities: List<City>) {
		//Do not need for this class
	}
}
