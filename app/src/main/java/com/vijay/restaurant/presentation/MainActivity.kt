package com.vijay.restaurant.presentation

import RestaurantAdapter
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vijay.restaurant.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        setupRecyclerView()
        observeViewModel()
        setupUIListeners()

        // Initial load
        viewModel.loadRestaurants(radius, API_KEY)
    }

    private fun setupRecyclerView() {
        adapter = RestaurantAdapter { viewModel.loadRestaurants(radius, API_KEY) }
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            setItemViewCacheSize(20)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter

            //Debounce scroll events to prevent multiple triggers for onScrolled
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                private var debounceJob: Job? = null

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) { // Only handle scrolling down
                        debounceJob?.cancel()
                        debounceJob = lifecycleScope.launch {
                            delay(200) // Adjust debounce delay
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val visibleItemCount = layoutManager.childCount
                            val totalItemCount = layoutManager.itemCount
                            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                            if (!viewModel.isLoading.value && !viewModel.isLastPage) {
                                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                                    viewModel.loadRestaurants(radius, API_KEY)
                                }
                            }
                        }
                    }
                }
            })

        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                viewModel.restaurants.collect { restaurantList ->
                    adapter.submitList(restaurantList)
                      //  binding.searchView.visibility = if (restaurantList.isNotEmpty()) View.VISIBLE else View.GONE
                }
                }
                launch {
                viewModel.error.collect { errorMessage ->
                        errorMessage?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.resetError()
                        }
                    }
                }
            }
        }
    }

    private fun setupUIListeners() {
        binding.radiusSlider.apply {
            max = 50
            progress = 5
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radius = progress * 100
                binding.radiusValueText.text = "$radius meters"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    viewModel.clearRestaurants()
                viewModel.loadRestaurants(radius, API_KEY)
            }
        })
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.clearRestaurants()
            viewModel.loadRestaurants(radius, API_KEY)
            binding.swipeRefresh.isRefreshing = false
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        viewModel.clearRestaurants()
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
                    viewModel.clearRestaurants()
                    viewModel.loadCurrentRestaurant("New York City",radius, API_KEY)
                }
            }
        }
                return true
            }
        })
    }

    companion object {
        const val API_KEY = "API_KEY"
    }
}

