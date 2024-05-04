package ru.dpav.weather.feature.cities_list

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_row_city.view.cityDetail
import kotlinx.android.synthetic.main.item_row_city.view.cityDetailCloudy
import kotlinx.android.synthetic.main.item_row_city.view.cityDetailPressure
import kotlinx.android.synthetic.main.item_row_city.view.cityDetailTemperature
import kotlinx.android.synthetic.main.item_row_city.view.cityDetailTitle
import kotlinx.android.synthetic.main.item_row_city.view.cityDetailWind
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.feature.city_details.CityDetailFragment
import ru.dpav.weather.util.Util

class CityHolder(
    itemView: View,
    private val mParentPresenter: ListPresenter,
) : RecyclerView.ViewHolder(itemView) {

    private var mCity: City? = null
    private var mTitle: TextView = itemView.cityDetailTitle
    private var mTemperature: TextView = itemView.cityDetailTemperature
    private var mWind: TextView = itemView.cityDetailWind
    private var mCloudy: TextView = itemView.cityDetailCloudy
    private var mPressure: TextView = itemView.cityDetailPressure
    private var mDetailInfo: ConstraintLayout = itemView.cityDetail

    fun bind(city: City, isOpened: Boolean) {
        mCity = city
        val context = mTitle.context as AppCompatActivity

        mTitle.text = city.name
        mTitle.setOnClickListener { onTitleClick() }

        with(mTemperature) {
            val icon = Util.Icons
                .getWeatherIconByName(city.weather[0].icon)
            text = context.getString(
                R.string.detail_temperature,
                city.main.temp.toInt()
            )
            setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
        }

        mWind.text = context.getString(
            R.string.detail_wind,
            city.wind.speed.toInt()
        )

        mCloudy.text = context.getString(
            R.string.detail_cloudy,
            city.clouds.cloudy
        )

        mPressure.text = context.getString(
            R.string.detail_pressure,
            Util.getPressureInMmHg(city.main.pressure)
        )

        with(mDetailInfo) {
            visibility = if (isOpened) View.VISIBLE else View.GONE
            setOnClickListener {
                val cityDetailFragment = CityDetailFragment.newInstance(city.id)
                cityDetailFragment.show(
                    context.supportFragmentManager,
                    "dialog_city2"
                )
            }
        }
    }

    private fun isDetailVisible(): Boolean =
        mDetailInfo.visibility == View.VISIBLE

    private fun onTitleClick() {
        val isVisible = isDetailVisible()
        mParentPresenter.onToggleDropDownInfo(adapterPosition, !isVisible)
    }
}
