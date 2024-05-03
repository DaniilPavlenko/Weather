package ru.dpav.weather

import ru.dpav.weather.api.City
import java.util.Observable

object CitiesRepository : Observable() {

    private const val CUSTOM_CITY_ID_OFFSET = 20000000

    var cities: List<City> = emptyList()
        set(cities) {
            field = cities
            notifySubscribers()
        }
    var customCities: ArrayList<City> = arrayListOf()
        set(cities) {
            field = cities
            notifySubscribers()
        }

    fun addCustomCity(city: City) {
        customCities.forEachIndexed { index, it ->
            if (it.id == city.id) {
                customCities[index] = city
                notifySubscribers()
                return
            }
        }
        customCities.add(city)
        notifySubscribers()
    }

    fun removeCustomCity(city: City) {
        customCities.remove(city)
        notifySubscribers()
    }

    private fun notifySubscribers() {
        setChanged()
        notifyObservers()
    }

    fun getNewCustomId(): Int {
        val lastId: Int = if (customCities.isNotEmpty()) {
            customCities.last().id
        } else {
            0
        }
        return CUSTOM_CITY_ID_OFFSET + lastId
    }

    fun isCustom(id: Int) = id >= CUSTOM_CITY_ID_OFFSET
}
