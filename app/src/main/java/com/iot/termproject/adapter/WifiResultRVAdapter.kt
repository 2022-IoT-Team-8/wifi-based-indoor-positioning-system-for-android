package com.iot.termproject.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iot.termproject.databinding.ItemWifiResultListBinding

/**
 * Wi-Fi list를 받아와 보여준다.
 *
 * @see com.iot.termproject.admin.SearchAccessPointActivity 에서 보여진다.
 */
class WifiResultRVAdapter(
    private val mContext: Context,
    private val mItemClickListener: MyItemClickListener
) : RecyclerView.Adapter<WifiResultRVAdapter.ViewHolder>() {
    private val wifiResults: ArrayList<ScanResult> = ArrayList<ScanResult>()

    // Click interface
    interface MyItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemWifiResultListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // ViewHolder binding
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(wifiResults[position])
    }

    // 데이터셋의 크기를 알려준다.
    override fun getItemCount(): Int = wifiResults.size

    // ViewHolder
    inner class ViewHolder(private val binding: ItemWifiResultListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(scanResult: ScanResult) {
            binding.itemWifiResultListBssidTv.text = "MAC: " + scanResult.BSSID
            binding.itemWifiResultListSsidTv.text = "SSID: " + scanResult.SSID
            binding.itemWifiResultListCapabilitiesTv.text = "Type: " + scanResult.capabilities
            binding.itemWifiResultFrequencyTv.text = "Frequency: " + scanResult.frequency.toString()
            binding.itemWifiResultListLevelTv.text = "RSSI: " + scanResult.level

            itemView.setOnClickListener {
                mItemClickListener.onItemClick(itemView, bindingAdapterPosition);
            }
        }
    }

    // 데이터셋 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addData(wifiResults: List<ScanResult>) {
        this.wifiResults.clear()
        this.wifiResults.addAll(wifiResults as ArrayList)
        notifyDataSetChanged()
    }
}
