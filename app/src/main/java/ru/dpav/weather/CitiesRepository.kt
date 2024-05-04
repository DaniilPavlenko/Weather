package ru.dpav.weather

import ru.dpav.weather.api.model.City
import java.util.Observable

object CitiesRepository : Observable() {

    var cities: List<City> = emptyList()
        set(cities) {
            field = cities
            setChanged()
            notifyObservers()
        }
}
