package ru.dpav.weather

import ru.dpav.weather.api.City
import java.util.*

object CitiesRepository : Observable() {
	var cities: List<City> = emptyList()
		set(cities) {
			field = cities
			setChanged()
			notifyObservers()
		}
	var customCities: ArrayList<City> = arrayListOf()
		set(cities) {
			field = cities
			setChanged()
			notifyObservers()
		}

	fun addCustomCity(city: City) {
		customCities.add(city)
		setChanged()
		notifyObservers()
	}
}