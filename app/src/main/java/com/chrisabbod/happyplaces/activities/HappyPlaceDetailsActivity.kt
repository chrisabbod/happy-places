package com.chrisabbod.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chrisabbod.happyplaces.databinding.ActivityHappyPlaceDetailsBinding
import com.chrisabbod.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailsActivity : AppCompatActivity() {

    private var binding: ActivityHappyPlaceDetailsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetailsModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            happyPlaceDetailsModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
            //This will not be in the format of a Happy Place Model so we have to cast it as one
        }

        if (happyPlaceDetailsModel != null) {
            setSupportActionBar(binding?.toolbarHappyPlaceDetails)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailsModel.title

            binding?.toolbarHappyPlaceDetails?.setNavigationOnClickListener {
                onBackPressed()
            }

            binding?.apply {
                ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailsModel.image))
                tvDescription.text =  happyPlaceDetailsModel.description
                tvLocation.text = happyPlaceDetailsModel.location
            }
        }
    }
}