package com.iot.termproject.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iot.termproject.data.entity.AccessPoint
import com.iot.termproject.databinding.ItemMainApBinding

/**
 * Main 화면에서 access point들을 보여준다.
 *
 * @see com.iot.termproject.admin.MainActivity 에서 보여진다.
 */
class MainAccessPointListRVAdapter(
    private val mContext: Context,
    private val mItemClickListener: MyItemClickListener
) : RecyclerView.Adapter<MainAccessPointListRVAdapter.ViewHolder>() {
    private val accessPoints = ArrayList<AccessPoint>()

    // Click interface
    interface MyItemClickListener {
        fun onItemClick(view: View, position: Int)      // 수정
        fun onItemLongClick()  // 삭제
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ItemMainApBinding = ItemMainApBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // ViewHolder binding
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(accessPoints[position])
    }

    // 데이터셋의 크기를 알려준다.
    override fun getItemCount(): Int = accessPoints.size

    // ViewHolder
    inner class ViewHolder(private val binding: ItemMainApBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(accessPoint: AccessPoint) {
            binding.itemMainApMacAddressTv.text = accessPoint.macAddress

            itemView.setOnClickListener {
                mItemClickListener.onItemClick(itemView, bindingAdapterPosition)
            }
        }
    }

    // 데이터셋 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addData(accessPoints: List<AccessPoint>) {
        this.accessPoints.clear()
        this.accessPoints.addAll(accessPoints as ArrayList)
        notifyDataSetChanged()
    }
}
