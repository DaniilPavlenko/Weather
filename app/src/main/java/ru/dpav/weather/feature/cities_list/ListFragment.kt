package ru.dpav.weather.feature.cities_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import ru.dpav.weather.R
import ru.dpav.weather.databinding.FragmentListBinding
import ru.dpav.weather.feature.city_details.CityDetailFragment

class ListFragment : Fragment(R.layout.fragment_list) {

    private val viewModel by viewModels<ListViewModel>()

    private var binding: FragmentListBinding? = null
    private var listAdapter: CitiesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listAdapter = CitiesAdapter(::navigateToDetails)
        binding = FragmentListBinding.bind(view).apply {
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
        binding = null
        listAdapter = null
    }

    private fun navigateToDetails(cityId: Int) {
        val detailsFragment = CityDetailFragment.newInstance(cityId)
        parentFragmentManager.commit {
            replace(R.id.mainFragmentContainer, detailsFragment, "details")
            addToBackStack("details")
        }
    }

    companion object {
        fun newInstance(): ListFragment = ListFragment()
    }
}
