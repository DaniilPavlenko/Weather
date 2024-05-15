package ru.dpav.weather.feature.cities_list

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.dpav.weather.R
import ru.dpav.weather.databinding.FragmentListBinding
import ru.dpav.weather.feature.city_details.CityDetailsFragment

class ListFragment : Fragment(R.layout.fragment_list) {

    private val viewModel by viewModels<ListViewModel>()

    private var listAdapter: CitiesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = CitiesAdapter(::navigateToDetails)
        FragmentListBinding.bind(view).apply {
            citiesRecyclerView.run {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(
                    DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                )
                adapter = listAdapter
            }
            val uiState = viewModel.uiState
            val cities = uiState.cities
            listAdapter?.setCities(cities)
            citiesNotFound.isVisible = cities.isEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null
    }

    private fun navigateToDetails(cityId: Int) {
        val detailsFragment = CityDetailsFragment.newInstance(cityId)
        parentFragmentManager.commit {
            replace(R.id.mainFragmentContainer, detailsFragment, "details")
            addToBackStack("details")
        }
    }

    companion object {
        fun newInstance(): ListFragment = ListFragment()
    }
}
