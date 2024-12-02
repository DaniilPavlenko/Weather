package ru.dpav.weather.feature.cities_list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.dpav.weather.R
import ru.dpav.weather.core.network.data.model.City
import ru.dpav.weather.databinding.ItemRowCityBinding
import ru.dpav.weather.ui.WeatherIconAssociator

class CitiesAdapter(
    private val onCityClick: (cityId: Int) -> Unit,
) : RecyclerView.Adapter<CitiesAdapter.CityHolder>() {

    private var cities: ArrayList<City> = arrayListOf()

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
        holder.bind(city)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCities(newCities: List<City>) {
        cities.clear()
        cities.addAll(newCities)
        notifyDataSetChanged()
    }

    inner class CityHolder(
        private val binding: ItemRowCityBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var attachedCityId: Int = 0

        init {
            binding.root.setOnClickListener { onCityClick(attachedCityId) }
        }

        fun bind(city: City) {
            attachedCityId = city.id
            with(binding) {
                cityDetailTitle.text = city.name
                with(cityDetailTemperature) {
                    text = context.getString(R.string.detail_temperature, city.main.temp.toInt())
                    val icon = WeatherIconAssociator.getIconByName(city.weather[0].icon)
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                }
            }
        }
    }
}
