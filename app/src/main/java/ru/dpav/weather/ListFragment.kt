package ru.dpav.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.fragment_list.view.citiesNotFound
import kotlinx.android.synthetic.main.fragment_list.view.citiesRecyclerView
import ru.dpav.weather.api.City
import ru.dpav.weather.presenters.ListPresenter
import ru.dpav.weather.views.ListView

class ListFragment : MvpAppCompatFragment(), ListView {

    private lateinit var mAdapter: CitiesAdapter
    private lateinit var mRecyclerView: RecyclerView

    @InjectPresenter
    lateinit var mListPresenter: ListPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_list,
            container,
            false
        )
        val viewManager = LinearLayoutManager(activity!!)
        mAdapter = CitiesAdapter(mListPresenter)
        mRecyclerView = view.citiesRecyclerView
        with(mRecyclerView) {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = mAdapter
        }
        return view
    }

    override fun updateCitiesList(cities: List<City>) {
        view?.citiesNotFound?.visibility =
            if (cities.isEmpty()) View.VISIBLE
            else View.GONE
        mAdapter.setCities(cities)
    }

    override fun toggleDropDownInfo(
        position: Int,
        shown: Boolean,
    ) {
        if (shown) {
            mAdapter.showDropDownInfo(position)
            mRecyclerView.smoothScrollToPosition(position + 1)
        } else {
            mAdapter.hideDropDownInfo()
        }
        mAdapter.notifyItemChanged(position)
    }

    companion object {
        fun newInstance(): ListFragment = ListFragment()
    }
}
