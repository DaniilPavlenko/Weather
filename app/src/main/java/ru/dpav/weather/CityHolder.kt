package ru.dpav.weather

import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.MvpDelegate
import com.arellomobile.mvp.MvpFacade
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import kotlinx.android.synthetic.main.item_row_city.view.*
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.ListPresenter
import ru.dpav.weather.util.Util
import ru.dpav.weather.views.ListView

class CityHolder(
	itemView: View,
	parentDelegate: MvpDelegate<*>,
	private val mParentPresenter: ListPresenter
) : MvpViewHolder(parentDelegate, itemView), ListView {

	private var mCity: City? = null
	private var mTitle: TextView = itemView.cityDetailTitle
	private var mTemperature: TextView = itemView.cityDetailTemperature
	private var mWind: TextView = itemView.cityDetailWind
	private var mCloudy: TextView = itemView.cityDetailCloudy
	private var mPressure: TextView = itemView.cityDetailPressure
	private var mDetailInfo: ConstraintLayout = itemView.cityDetail

	@InjectPresenter
	lateinit var mHolderPresenter: ListPresenter

	@ProvidePresenter
	fun provideListPresenter(): ListPresenter {
		MvpFacade.getInstance()
		return ListPresenter(mCity!!.id)
	}

	fun bind(city: City, isOpened: Boolean) {
		destroyMvpDelegate()
		mCity = city
		createMvpDelegate()
		val context = mTitle.context as AppCompatActivity

		mTitle.text = city.name
		mTitle.setOnClickListener { onTitleClick() }

		with(mTemperature) {
			val icon = Util.getWeatherIconByName(city.weather[0].icon)
			text = context.getString(R.string.detail_temperature, city.main.temp.toInt())
			setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
		}

		mWind.text = context.getString(R.string.detail_wind, city.wind.speed.toInt())

		mCloudy.text = context.getString(R.string.detail_cloudy, city.clouds.cloudy)

		mPressure.text = context.getString(R.string.detail_pressure,
			Util.getPressureInMmHg(city.main.pressure))

		with(mDetailInfo) {
			visibility = if (isOpened) View.VISIBLE else View.GONE
			setOnClickListener {
				val cityDetailFragment = CityDetailFragment.newInstance(city)
				cityDetailFragment.show(context.supportFragmentManager, "dialog_city2")
			}
		}
	}

	private fun isDetailVisible(): Boolean =
		mDetailInfo.visibility == View.VISIBLE

	override fun getMvpChildId(): String? {
		return if (mCity == null) null else mCity!!.id.toString()
	}

	private fun onTitleClick() {
		val isVisible = isDetailVisible()
		mHolderPresenter.onToggleDropDownInfo(adapterPosition, !isVisible)
		mParentPresenter.onToggleDropDownInfo(adapterPosition, !isVisible)
	}

	override fun toggleDropDownInfo(position: Int, shown: Boolean) {
		mDetailInfo.visibility =
			if (shown) View.VISIBLE
			else View.GONE
	}

	override fun updateCitiesList(cities: List<City>) {}
}
