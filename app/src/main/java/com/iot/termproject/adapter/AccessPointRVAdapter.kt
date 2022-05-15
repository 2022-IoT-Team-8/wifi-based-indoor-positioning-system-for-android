package com.iot.termproject.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iot.termproject.data.entity.AccessPoint
import com.iot.termproject.databinding.ItemAccessPointListBinding

/**
 * 'RoomPointActivity'에서 access point들을 보여준다.
 *
 * @see com.iot.termproject.admin.RoomPointActivity 에서 보여진다.
 */
class AccessPointRVAdapter() : RecyclerView.Adapter<AccessPointRVAdapter.ViewHolder>() {
    private var accessPoints = ArrayList<AccessPoint>()

    // ViewHolder 생성
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemAccessPointListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // ViewHolder binding
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accessPoints[position])
    }

    // 데이터셋의 크기를 알려준다.
    override fun getItemCount(): Int = accessPoints.size

    // ViewHolder
    inner class ViewHolder(private val binding: ItemAccessPointListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(accessPoint: AccessPoint) {
            binding.itemAccessPointListBssidTv.text = accessPoint.bssid
            binding.itemAccessPointListSsidTv.text = accessPoint.ssid
            binding.itemAccessPointListMeanRssTv.text = accessPoint.meanRss.toString()
        }
    }

    // 데이터셋 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addData(accessPointList: ArrayList<AccessPoint>) {
        this.accessPoints.clear()
        this.accessPoints.addAll(accessPointList)
        notifyDataSetChanged()
    }
}