package com.chrisabbod.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chrisabbod.happyplaces.databinding.ActivityHappyPlaceDetailsBinding

class HappyPlaceDetailsActivity : AppCompatActivity() {

    private var binding: ActivityHappyPlaceDetailsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        
    }
}