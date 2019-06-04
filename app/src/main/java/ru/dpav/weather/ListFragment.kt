package ru.dpav.weather

import android.animation.Animator
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.dpav.weather.api.City
import ru.dpav.weather.util.Util

class ListFragment: Fragment(), MapFragment.OnWeatherGotCallback {
    private lateinit var mRecyclerView: RecyclerView
    private var mCities: ArrayList<City> = ArrayList()
    private var savedOpenedPosition: Int = -1

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        mRecyclerView = view.findViewById(R.id.cities_recycler_view)
        val viewManager = LinearLayoutManager(activity)
        val viewAdapter = CitiesAdapter(savedOpenedPosition, mRecyclerView, mCities)
        if (savedInstanceState != null) {
            savedOpenedPosition = savedInstanceState.getInt(ARG_OPENED_POSITION, -1)
        }
        with(mRecyclerView) {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_OPENED_POSITION, (mRecyclerView.adapter as CitiesAdapter).openedPosition)
    }

    override fun onWeatherGot(cities: List<City>?) {
        cities?.let {
            (mRecyclerView.adapter as CitiesAdapter).setCities(cities)
        }
        if (savedOpenedPosition != -1) {
            (mRecyclerView.adapter as CitiesAdapter).openedPosition = savedOpenedPosition
            mRecyclerView.smoothScrollToPosition(savedOpenedPosition)
            savedOpenedPosition = -1
        }
    }

    private class CitiesAdapter(var openedPosition: Int, private var recyclerView: RecyclerView,
        private var cities: ArrayList<City>): RecyclerView.Adapter<CitiesAdapter.CityHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, index: Int): CityHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_city, parent, false)
            return CityHolder(view)
        }

        override fun getItemCount(): Int {
            return cities.count()
        }

        override fun onBindViewHolder(holder: CityHolder, index: Int) {
            val city = cities[index]
            holder.bind(city, openedPosition == index) {position ->
                if (openedPosition > -1) {
                    val cityHolder = recyclerView.findViewHolderForAdapterPosition(openedPosition) as? CityHolder
                    cityHolder?.closeDetail()
                    cityHolder?.resetTitleBackground()
                }
                openedPosition = position
                recyclerView.smoothScrollToPosition(position + 1)
            }
        }

        fun setCities(newCities: List<City>) {
            cities.clear()
            cities.addAll(newCities)
            openedPosition = -1
            notifyDataSetChanged()
        }

        class CityHolder(item: View): RecyclerView.ViewHolder(item) {
            private var mTitle: TextView = item.findViewById(R.id.city_detail_title)
            private var mTemperature: TextView = item.findViewById(R.id.city_detail_temperature)
            private var mWind: TextView = item.findViewById(R.id.city_detail_wind)
            private var mCloudy: TextView = item.findViewById(R.id.city_detail_cloudy)
            private var mPressure: TextView = item.findViewById(R.id.city_detail_pressure)
            private var mDetailInfo: ConstraintLayout = item.findViewById(R.id.city_detail)

            fun animateDetail() {
                val isShown = mDetailInfo.visibility == View.VISIBLE
                val translationY = if (isShown) -mDetailInfo.height.toFloat() else 0f
                if (!isShown) {
                    mDetailInfo.translationY = -300f
                }
                mDetailInfo.animate()
                    .translationY(translationY)
                    .setListener(object: Animator.AnimatorListener {
                        override fun onAnimationCancel(p0: Animator?) {}
                        override fun onAnimationRepeat(p0: Animator?) {}

                        override fun onAnimationEnd(p0: Animator?) {
                            if (isShown) {
                                mDetailInfo.visibility = View.GONE
                                mTitle.isSelected = false
                            }
                        }

                        override fun onAnimationStart(p0: Animator?) {
                            if (!isShown) {
                                mDetailInfo.visibility = View.VISIBLE
                                mTitle.isSelected = true
                            }
                        }
                    })
            }

            fun bind(city: City, isOpened: Boolean, clickCallback: (Int) -> Unit) {
                mTitle.text = city.name
                mTitle.setOnClickListener {
                    animateDetail()
                    clickCallback(this.adapterPosition)
                }
                if (isOpened) {
                    mTitle.isSelected = true
                    mDetailInfo.visibility = View.VISIBLE
                } else {
                    mTitle.isSelected = false
                    mDetailInfo.visibility = View.GONE
                }
                val context = mTitle.context
                mTemperature.text = context.getString(R.string.detail_temperature, city.main!!.temp.toInt())
                val icon = Util.getWeatherIconByName(city.weather?.get(0)!!.icon)
                mTemperature.setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
                mWind.text = context.getString(R.string.detail_wind, city.wind!!.speed.toInt())
                mCloudy.text = context.getString(R.string.detail_cloudy, city.clouds!!.all)
                mPressure.text = context.getString(R.string.detail_pressure, city.main.pressure.toInt())
                mDetailInfo.setOnClickListener {
                    val cityDetailFragment = CityDetailFragment.newInstance(city)
                    cityDetailFragment.show((context as AppCompatActivity).supportFragmentManager, "dialog_city2")
                }
            }

            fun closeDetail() {
                mDetailInfo.visibility = View.GONE
            }

            fun resetTitleBackground() {
                mTitle.isSelected = false
            }
        }
    }

    companion object {
        private const val ARG_OPENED_POSITION: String = "opened_position"

        @JvmStatic
        fun newInstance(): ListFragment {
            return ListFragment()
        }
    }
}