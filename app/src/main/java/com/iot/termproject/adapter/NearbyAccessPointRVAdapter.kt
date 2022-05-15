package com.iot.termproject.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iot.termproject.data.LocationDistance
import com.iot.termproject.databinding.ItemNearbyAccessPointBinding

/**
 * @see com.iot.termproject.user.LocateMeActivity
 */
class NearbyAccessPointRVAdapter : RecyclerView.Adapter<NearbyAccessPointRVAdapter.ViewHolder>() {
    private var accessPoints = ArrayList<LocationDistance>()

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNearbyAccessPointBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // ViewHolder binding
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accessPoints[position])
    }

    // 데이터셋의 크기를 알려준다.
    override fun getItemCount(): Int = accessPoints.size

    // ViewHolder
    inner class ViewHolder(private val binding: ItemNearbyAccessPointBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(accessPoint: LocationDistance) {
            binding.itemNearbyAccessPointSsid.text = accessPoint.name
            binding.itemNearbyAccessPointBssid.text = accessPoint.location
            binding.itemNearbyAccessPointLevel.text = accessPoint.distance.toString()
        }
    }

    // 데이터셋 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addData(accessPoints: List<LocationDistance>) {
        this.accessPoints.clear()
        this.accessPoints.addAll(accessPoints)
        notifyDataSetChanged()
    }
}