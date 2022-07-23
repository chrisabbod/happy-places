package com.chrisabbod.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chrisabbod.happyplaces.activities.AddHappyPlaceActivity
import com.chrisabbod.happyplaces.activities.MainActivity
import com.chrisabbod.happyplaces.database.DatabaseHandler
import com.chrisabbod.happyplaces.databinding.ItemHappyPlaceBinding
import com.chrisabbod.happyplaces.models.HappyPlaceModel

open class HappyPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<HappyPlacesAdapter.HappyPlacesViewHolder>() {

    //Step 2 - create the following private global variable
    private var onClickListener: OnClickListener? = null

    inner class HappyPlacesViewHolder(binding: ItemHappyPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val ivPlaceImage = binding.ivPlaceImage
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HappyPlacesViewHolder {
        return HappyPlacesViewHolder(ItemHappyPlaceBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    //Step 3 - Create a function that binds the onClickListener
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: HappyPlacesViewHolder, position: Int) {
        val model = list[position]

        if (holder is HappyPlacesViewHolder) {
            holder.ivPlaceImage.setImageURI(Uri.parse(model.image))
            holder.tvTitle.text = model.title
            holder.tvDescription.text = model.description

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    //Passing position (which element was clicked)
                    // and model (our HappyPlaceModel so we pass all the data to the next activity)
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    fun removeAt(position: Int) {
        val dbHandler = DatabaseHandler(context)
        val isDelete = dbHandler.deleteHappyPlace(list[position])

        if (isDelete > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    //Add click functionality to the recyclerview list items in 5 steps
    //This all needs to be done because an adapter cannot have an onClickListener which is why we
    //need to do this little workaround.
    //Step 1 - Create an OnClickListener interface inside the adapter
    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }
}