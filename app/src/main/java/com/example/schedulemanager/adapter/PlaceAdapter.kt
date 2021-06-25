package com.example.schedulemanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.schedulemanager.data.location.DocumentsVO
import com.example.schedulemanager.databinding.ItemSearchPlaceBinding
import com.example.schedulemanager.lisetener.OnSearchPlaceDialogItemClickListener

/**
 * 위치 목록 RecyclerView Adapter
 */
class PlaceAdapter : RecyclerView.Adapter<PlaceAdapter.LocationItemViewHolder>() {

    companion object {
        const val NO_DATA = 0
    }

    lateinit var onSearchPlaceDialogItemClickListener: OnSearchPlaceDialogItemClickListener
    var documentList = arrayListOf<DocumentsVO>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationItemViewHolder {
        val binding =
            ItemSearchPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return documentList.size
    }

    override fun onBindViewHolder(holder: LocationItemViewHolder, position: Int) {
        holder.binding.tvSearchPlaceName.text = documentList[position].place_name
        holder.binding.tvSearchPlaceAddr.text = documentList[position].address_name

        holder.itemView.setOnClickListener {
            onSearchPlaceDialogItemClickListener.onPlaceDialogItemClickListener(
                it,
                documentList[position]
            )
        }
    }

    /**
     * 위치아이템 뷰홀더
     */
    inner class LocationItemViewHolder(val binding: ItemSearchPlaceBinding) :
        RecyclerView.ViewHolder(binding.root)
}