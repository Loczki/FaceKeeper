/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.facedetection

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.mediapipe.examples.facedetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var gestureDetector: GestureDetectorCompat
    private var hasSwipedRight = false
    private var hasSwipedLeft = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        // Uncomment if using bottom navigation
        // activityMainBinding.navigation.setupWithNavController(navController)

        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            if (Math.abs(diffX) > Math.abs(diffY) &&
                Math.abs(diffX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                if (diffX > 0) {
                    onRightSwipe()
                } else {
                    onLeftSwipe()
                }
                return true
            }
            return false
        }
    }

    private fun onRightSwipe() {
        if (hasSwipedRight) return
        hasSwipedRight = true
        hasSwipedLeft = false

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left)     // New fragment slides in from left
            .setExitAnim(R.anim.slide_out_right)    // Current fragment slides out to right
            .setPopEnterAnim(R.anim.slide_in_right) // When coming back, slide in from right
            .setPopExitAnim(R.anim.slide_out_left)  // When going back, slide out to left
            .build()

        navController.navigate(R.id.secondFragment, null, navOptions)
    }

    private fun onLeftSwipe() {
        if (hasSwipedLeft) return
        hasSwipedLeft = true
        hasSwipedRight = false

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)    // New fragment slides in from right
            .setExitAnim(R.anim.slide_out_left)     // Current fragment slides out to left
            .setPopEnterAnim(R.anim.slide_in_left)  // Back: slide in from left
            .setPopExitAnim(R.anim.slide_out_right) // Back: slide out to right
            .build()

        navController.navigate(R.id.camera_fragment, null, navOptions)
    }

    override fun onBackPressed() {
        finish()
    }
}