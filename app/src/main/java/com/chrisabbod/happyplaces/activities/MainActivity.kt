package com.chrisabbod.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.chrisabbod.happyplaces.adapters.HappyPlacesAdapter
import com.chrisabbod.happyplaces.database.DatabaseHandler
import com.chrisabbod.happyplaces.databinding.ActivityMainBinding
import com.chrisabbod.happyplaces.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            getHappyPlacesListFromLocalDb()
        } else {
            Log.e("Activity", "Cancelled or Back Pressed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            resultLauncher.launch(intent)
        }
        getHappyPlacesListFromLocalDb()
    }

    private fun setupHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>) {
        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
        binding?.rvHappyPlacesList?.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter

        //Step 4 - Use setOnClickListener on our adapter and then override the onClick function
        //we created in the adapter class
        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(
                    this@MainActivity,
                    HappyPlaceDetailsActivity::class.java
                )
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
    }

    private fun getHappyPlacesListFromLocalDb() {
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if (getHappyPlaceList.isNotEmpty()) {
            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlaceList)
        } else {
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    companion object {
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}