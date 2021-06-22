package com.example.schedulemanager.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calendarapp.databinding.ItemSearchPlaceBinding
import com.example.calendarapp.lisetener.OnClickListener
import com.example.calendarapp.DocumentsVO


/**
 * 위치 목록 RecyclerView Adapter
 */
class PlaceAdapter : RecyclerView.Adapter<PlaceAdapter.LocationItemViewHolder>() {

    companion object {
        const val NO_DATA = 0
    }

    var documentList = arrayListOf<DocumentsVO>()
    lateinit var onClickListener: OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationItemViewHolder {
        val binding =
            ItemSearchPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (documentList != null) documentList.size else NO_DATA
    }

    override fun onBindViewHolder(holder: LocationItemViewHolder, position: Int) {
        holder.binding.tvSearchPlaceName.text = documentList.get(position).place_name
        holder.binding.tvSearchPlaceAddr.text = documentList.get(position).address_name

        holder.itemView.setOnClickListener(View.OnClickListener {
            onClickListener.onClickListener(it, documentList.get(position))
        })
    }

    inner class LocationItemViewHolder(val binding: ItemSearchPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {}
}