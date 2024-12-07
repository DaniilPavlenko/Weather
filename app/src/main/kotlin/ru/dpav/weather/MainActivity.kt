package ru.dpav.weather

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.dpav.weather.core.navigation.Navigator
import ru.dpav.weather.core.navigation.NavigatorProvider
import ru.dpav.weather.navigation.NavigatorImpl

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigatorProvider {

    private val navigator: Navigator by lazy { NavigatorImpl() }

    override fun provideNavigator(): Navigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView((R.layout.activity_main))
    }

}
