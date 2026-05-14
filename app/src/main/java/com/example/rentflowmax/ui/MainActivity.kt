package com.example.rentflowmax.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.rentflowmax.R
import com.example.rentflowmax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val topLevelDestinations = setOf(
        R.id.dashboardFragment,
        R.id.propertyListFragment,
        R.id.tenantListFragment,
        R.id.paymentListFragment,
        R.id.maintenanceListFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val gesture = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
            v.updatePadding(bottom = maxOf(bars.bottom, gesture.bottom))
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(topLevelDestinations)
        setupActionBarWithNavController(navController, appBarConfig)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Limpia todo el back stack hasta el start destination.
            navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
            if (item.itemId != navController.graph.startDestinationId) {
                val options = NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(navController.graph.startDestinationId, inclusive = false)
                    .build()
                try {
                    navController.navigate(item.itemId, null, options)
                } catch (e: IllegalArgumentException) {
                    return@setOnItemSelectedListener false
                }
            }
            true
        }

        binding.bottomNavigation.setOnItemReselectedListener {
            // Si reseleccionan el tab activo, popea hasta el start (o el tab actual si no es start)
            navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val matched = destination.hierarchy.firstOrNull { d ->
                binding.bottomNavigation.menu.findItem(d.id) != null
            }
            matched?.let { binding.bottomNavigation.menu.findItem(it.id)?.isChecked = true }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
