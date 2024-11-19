package com.vijay.restaurant.presentation

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.vijay.restaurant.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: RestaurantViewModel by viewModels()
    private lateinit var adapter: RestaurantAdapter
    private var radius = 500
    private var searchLocation: Job? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RestaurantAdapter { loadMoreRestaurants() }
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                viewModel.restaurants.collect { restaurantList ->
                    adapter.submitList(restaurantList)

                    if (restaurantList.isNotEmpty()) {
                            binding.searchView.visibility = View.VISIBLE
                    }
                }
                }
                launch {
                viewModel.error.collect { errorMessage ->
                    if (errorMessage != null) {
                        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                            viewModel.resetError()
                        }
                    }
                }
            }
        }

        binding.radiusSlider.max = 50
        binding.radiusSlider.progress = 5

        binding.radiusSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radius = progress * 100
                binding.radiusValueText.text = "$radius meters"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.loadRestaurants(radius, API_KEY)
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadRestaurants(radius, API_KEY)
            binding.swipeRefresh.isRefreshing = false
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        adapter.submitList(emptyList())
                        viewModel.searchRestaurants(it, radius, API_KEY)
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchLocation?.cancel()
                searchLocation = lifecycleScope.launch {
                    delay(500)
               newText?.let {
                        if (it.length >= 2) {
                    viewModel.searchRestaurants(it, radius, API_KEY)
                } else if (it.isBlank()) {
                    viewModel.loadRestaurants(radius, API_KEY)
                }
            }
        }
                return true
            }
        })

        viewModel.loadRestaurants(radius, API_KEY)
    }

    private fun loadMoreRestaurants() {
        viewModel.loadRestaurants(radius, API_KEY)
    }

    companion object {
        const val API_KEY = "API_KEY"
    }
}

