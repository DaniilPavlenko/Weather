package ru.dpav.weather

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.dpav.weather.core.navigation.Navigator
import ru.dpav.weather.core.navigation.NavigatorProvider
import ru.dpav.weather.navigation.NavigatorImpl

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main), NavigatorProvider {

    private val navigator: Navigator by lazy { NavigatorImpl() }

    override fun provideNavigator(): Navigator = navigator

}
