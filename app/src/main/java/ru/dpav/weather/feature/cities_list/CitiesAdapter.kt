package ru.dpav.weather.feature.cities_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.databinding.ItemRowCityBinding
import ru.dpav.weather.ui.WeatherIconAssociator

class CitiesAdapter(
    private val onTitleClick: (position: Int, isDroppedDown: Boolean) -> Unit,
    private val onDetailsClick: (cityId: Int) -> Unit,
) : RecyclerView.Adapter<CitiesAdapter.CityHolder>() {

    private var cities: ArrayList<City> = arrayListOf()
    private var openedItemPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int,
    ): CityHolder {
        val binding = ItemRowCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityHolder(binding)
    }

    override fun getItemCount(): Int = cities.count()

    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        val city = cities[position]
        val isOpened = position == openedItemPosition
        holder.bind(city, isOpened)
    }

    fun hideDropDownInfo() {
        openedItemPosition = -1
    }

    fun showDropDownInfo(position: Int) {
        notifyItemChanged(openedItemPosition)
        openedItemPosition = position
    }

    fun setCities(newCities: List<City>) {
        hideDropDownInfo()
        cities.clear()
        cities.addAll(newCities)
        notifyDataSetChanged()
    }

    inner class CityHolder(
        private val binding: ItemRowCityBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var bindedCity: City
        private val context = itemView.context.applicationContext

        init {
            binding.cityDetailTitle.setOnClickListener {
                onTitleClick(adapterPosition, adapterPosition == openedItemPosition)
            }
            binding.cityDetail.setOnClickListener { onDetailsClick(bindedCity.id) }
        }

        fun bind(city: City, isOpened: Boolean) {
            bindedCity = city

            binding.run {
                cityDetailTitle.text = city.name

                val icon = WeatherIconAssociator.getIconByName(city.weather[0].icon)
                cityDetailTemperature.run {
                    text = context.getString(R.string.detail_temperature, city.main.temp.toInt())
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                }

                cityDetailWind.text =
                    context.getString(R.string.detail_wind, city.wind.speed.toInt())
                cityDetailCloudy.text =
                    context.getString(R.string.detail_cloudy, city.clouds.cloudy)
                cityDetailPressure.text =
                    context.getString(R.string.detail_pressure, city.main.pressureInMmHg)


                cityDetail.visibility = if (isOpened) View.VISIBLE else View.GONE
            }
        }
    }
}
