package ru.dpav.weather.feature.cities_list.impl

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import ru.dpav.weather.common.ui.extension.consumeWindowInsets
import ru.dpav.weather.common.ui.extension.popBackStackToRoot
import ru.dpav.weather.core.navigation.findFeatureProvider
import ru.dpav.weather.core.navigation.findNavigator
import ru.dpav.weather.feature.cities_list.impl.databinding.FragmentListBinding
import ru.dpav.weather.feature.details.api.DetailsFeatureProvider

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
            root.consumeWindowInsets(systemBars() or displayCutout()) { insets ->
                toolbar.updatePadding(
                    left = insets.left,
                    top = insets.top,
                    right = insets.right
                )
                citiesRecyclerView.updatePadding(
                    left = insets.left,
                    right = insets.right,
                    bottom = insets.bottom
                )
            }
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
        val detailsFragment = findFeatureProvider<DetailsFeatureProvider>().get(cityId)
        findNavigator().run {
            navigateTo(destinationFragment = detailsFragment, tag = "details")
        }
    }

    companion object {
        fun newInstance(): ListFragment = ListFragment()
    }
}
