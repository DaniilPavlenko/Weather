package ru.dpav.weather.feature.cities_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ru.dpav.weather.R
import ru.dpav.weather.common.ui.extension.popBackStackToRoot
import ru.dpav.weather.core.navigation.findFeatureProvider
import ru.dpav.weather.databinding.FragmentListBinding
import ru.dpav.weather.feature.details.api.StupidDetailsFeatureProvider

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list) {

    private val viewModel by viewModels<ListViewModel>()

    private var listAdapter: CitiesAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val citiesWeather = viewModel.uiState.citiesWeather

        if (citiesWeather.isEmpty()) {
            parentFragmentManager.popBackStackToRoot()
            return
        }

        listAdapter = CitiesAdapter(::navigateToDetails).apply {
            setCities(citiesWeather)
        }
        FragmentListBinding.bind(view).apply {
            toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
            citiesRecyclerView.run {
                setHasFixedSize(true)
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                adapter = listAdapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null
    }

    private fun navigateToDetails(cityId: Int) {
        val detailsFragment = findFeatureProvider<StupidDetailsFeatureProvider>().get(cityId)
        parentFragmentManager.commit {
            replace(R.id.mainFragmentContainer, detailsFragment, "details")
            addToBackStack("details")
        }
    }

    companion object {
        fun newInstance(): ListFragment = ListFragment()
    }
}
