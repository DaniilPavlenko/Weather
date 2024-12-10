package ru.dpav.weather.feature.cities_list.impl

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.dpav.weather.common.ui.WeatherIconAssociator
import ru.dpav.weather.core.model.CityWeather
import ru.dpav.weather.feature.cities_list.impl.databinding.ItemRowCityBinding
import ru.dpav.weather.common.strings.R as StringsR

internal class CitiesAdapter(
    private val onCityClick: (cityId: Int) -> Unit,
) : RecyclerView.Adapter<CitiesAdapter.CityHolder>() {

    private var citiesWeather: ArrayList<CityWeather> = arrayListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int,
    ): CityHolder {
        val binding = ItemRowCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityHolder(binding)
    }

    override fun getItemCount(): Int = citiesWeather.count()

    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        val cityWeather = citiesWeather[position]
        holder.bind(cityWeather)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCities(newCitiesWeather: List<CityWeather>) {
        citiesWeather.clear()
        citiesWeather.addAll(newCitiesWeather)
        notifyDataSetChanged()
    }

    inner class CityHolder(
        private val binding: ItemRowCityBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var attachedCityId: Int = 0

        init {
            binding.root.setOnClickListener { onCityClick(attachedCityId) }
        }

        fun bind(cityWeather: CityWeather) {
            attachedCityId = cityWeather.cityId
            with(binding) {
                cityDetailTitle.text = cityWeather.cityName
                with(cityDetailTemperature) {
                    text = context.getString(
                        StringsR.string.detail_temperature,
                        cityWeather.temperature
                    )
                    val icon = WeatherIconAssociator.getIconByWeatherType(
                        weatherType = cityWeather.weatherType,
                        isNight = cityWeather.isNight
                    )
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                }
            }
        }
    }
}
