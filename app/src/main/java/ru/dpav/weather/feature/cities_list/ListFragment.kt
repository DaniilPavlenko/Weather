package ru.dpav.weather.feature.cities_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import ru.dpav.weather.R
import ru.dpav.weather.api.model.City
import ru.dpav.weather.databinding.FragmentListBinding
import ru.dpav.weather.feature.city_details.CityDetailFragment

class ListFragment : MvpAppCompatFragment(), ListView {

    @InjectPresenter
    lateinit var presenter: ListPresenter

    private var binding: FragmentListBinding? = null
    private var listAdapter: CitiesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListBinding.bind(view)

        listAdapter = CitiesAdapter(
            onTitleClick = { position, isDroppedDown ->
                presenter.onToggleDropDownInfo(position, !isDroppedDown)
            },
            onDetailsClick = { cityId ->
                val detailsFragment = CityDetailFragment.newInstance(cityId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentContainer, detailsFragment, "details")
                    .addToBackStack("details")
                    .commit()
            }
        )

        binding?.citiesRecyclerView?.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        listAdapter = null
    }

    override fun updateCitiesList(cities: List<City>) {
        binding?.citiesNotFound?.visibility = if (cities.isEmpty()) View.VISIBLE else View.GONE
        listAdapter?.setCities(cities)
    }

    override fun toggleDropDownInfo(position: Int, shown: Boolean) {
        listAdapter?.run {
            if (shown) {
                showDropDownInfo(position)
                binding?.citiesRecyclerView?.smoothScrollToPosition(position + 1)
            } else {
                hideDropDownInfo()
            }
            notifyItemChanged(position)
        }

    }

    companion object {
        fun newInstance(): ListFragment = ListFragment()
    }
}
