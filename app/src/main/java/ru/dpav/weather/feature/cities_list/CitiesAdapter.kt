package ru.dpav.weather.feature.cities_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City

class CitiesAdapter(
    private var mListPresenter: ListPresenter,
) : RecyclerView.Adapter<CityHolder>() {

    private var mCities: ArrayList<City> = arrayListOf()
    private var mOpenedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int,
    ): CityHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_row_city,
            parent,
            false
        )
        return CityHolder(view, mListPresenter)
    }

    override fun getItemCount(): Int {
        return mCities.count()
    }

    override fun onBindViewHolder(
        holder: CityHolder,
        position: Int,
    ) {
        val city = mCities[position]
        val isOpened = position == mOpenedPosition
        holder.bind(city, isOpened)
    }

    fun hideDropDownInfo() {
        mOpenedPosition = -1
    }

    fun showDropDownInfo(position: Int) {
        this.notifyItemChanged(mOpenedPosition)
        mOpenedPosition = position
    }

    fun setCities(newCities: List<City>) {
        hideDropDownInfo()
        mCities.clear()
        mCities.addAll(newCities)
        notifyDataSetChanged()
    }
}
