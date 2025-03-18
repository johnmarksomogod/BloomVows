package com.example.plannerwedding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val heartIds = intArrayOf(R.id.heart1, R.id.heart2, R.id.heart3, R.id.heart4, R.id.heart5)
    private var heartViews = ArrayList<ImageView>()
    private var currentHeartIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isLoaderVisible = false
    private lateinit var heartRunnable: Runnable
    private var loaderView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Get NavController from NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        // Connect Bottom Navigation to NavController
        bottomNavigationView.setupWithNavController(navController)

        // Hide Bottom Navigation on login/signup/setup screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.getStarted,
                R.id.loginFragment,
                R.id.signUpFragment,
                R.id.forgotPassword,
                R.id.signUpFragment,
                R.id.enterDateFragment,
                R.id.enterBudgetFragment,
                R.id.enterVenueFragment -> bottomNavigationView.visibility = View.GONE
                else -> bottomNavigationView.visibility = View.VISIBLE
            }
        }

        initializeHeartAnimation()
    }

    private fun initializeHeartAnimation() {
        heartRunnable = Runnable {
            if (isLoaderVisible && heartViews.isNotEmpty()) {
                try {
                    // Reset previous heart
                    if (currentHeartIndex > 0) {
                        heartViews[currentHeartIndex - 1].setImageResource(R.drawable.heart_outline)
                    }

                    // Fill current heart
                    if (currentHeartIndex < heartViews.size) {
                        heartViews[currentHeartIndex].setImageResource(R.drawable.heartt)
                        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.heart_pulse)
                        heartViews[currentHeartIndex].startAnimation(pulseAnimation)
                        currentHeartIndex++
                    }

                    // Reset to first heart when reaching the end
                    if (currentHeartIndex >= heartViews.size) {
                        currentHeartIndex = 0
                        // Reset all hearts to outline
                        for (heart in heartViews) {
                            heart.setImageResource(R.drawable.heart_outline)
                        }
                    }

                    // Continue the animation
                    handler.postDelayed(heartRunnable, 800)
                } catch (e: Exception) {
                    // Handle potential exceptions silently
                    e.printStackTrace()
                }
            }
        }
    }

    // Show the loader
    fun showLoader() {
        try {
            val loaderContainer = findViewById<FrameLayout>(R.id.loaderContainer)

            // Create new loader view
            loaderView = layoutInflater.inflate(R.layout.loader, loaderContainer, false)

            // Clear container and add the new view
            loaderContainer.removeAllViews()
            loaderContainer.addView(loaderView)


            // Clear previous heart views and initialize new ones
            heartViews.clear()
            for (id in heartIds) {
                loaderView?.findViewById<ImageView>(id)?.let {
                    heartViews.add(it)
                }
            }

            // Reset animation state
            currentHeartIndex = 0
            isLoaderVisible = true

            // Start the heart animation if we have hearts
            if (heartViews.isNotEmpty()) {
                handler.post(heartRunnable)
            }

            // Show the loader container
            loaderContainer.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Hide the loader
    fun hideLoader() {
        try {
            val loaderContainer = findViewById<FrameLayout>(R.id.loaderContainer)
            loaderContainer.visibility = View.GONE
            isLoaderVisible = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove any pending callbacks to prevent memory leaks
        handler.removeCallbacks(heartRunnable)
    }
}