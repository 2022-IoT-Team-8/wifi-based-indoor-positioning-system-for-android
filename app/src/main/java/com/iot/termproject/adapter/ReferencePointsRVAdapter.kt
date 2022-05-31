package com.iot.termproject.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iot.termproject.data.entity.ReferencePoint
import com.iot.termproject.databinding.ItemMainRpBinding

/**
 * Main에서 room point들을 보여준다.
 *
 * @see com.iot.termproject.ui.admin.MainActivity 에서 보여진다.
 */
class ReferencePointsRVAdapter(
    private val mContext: Context,
    private val mItemClickListener: MyItemClickListener
) : RecyclerView.Adapter<ReferencePointsRVAdapter.ViewHolder>() {
    private val referencePoints = ArrayList<ReferencePoint>()

    // Click listener
    interface MyItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onItemLongClick(position: Int)
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ItemMainRpBinding =
            ItemMainRpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // ViewHolder binding
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(referencePoints[position])
    }

    // 데이터셋 크기를 알려준다.
    override fun getItemCount(): Int = referencePoints.size

    // ViewHolder
    inner class ViewHolder(private val binding: ItemMainRpBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(referencePoint: ReferencePoint) {
            binding.itemMainRpNameTv.text = referencePoint.name.toString() + "호"
            binding.itemMainRpFloorTv.text = referencePoint.floor.toString()
            binding.itemMainRpLatitudeTv.text = referencePoint.latitude.toString()
            binding.itemMainRpLongitudeTv.text = referencePoint.longitude.toString()

            itemView.setOnClickListener {
                mItemClickListener.onItemClick(itemView, bindingAdapterPosition)
            }

            itemView.setOnLongClickListener {
                mItemClickListener.onItemLongClick(bindingAdapterPosition)
                removePoint(bindingAdapterPosition)
                return@setOnLongClickListener false
            }
        }
    }

    // 데이터셋 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addData(referencePoints: List<ReferencePoint>) {
        this.referencePoints.clear()
        this.referencePoints.addAll(referencePoints as ArrayList)
        notifyDataSetChanged()
    }

    private fun removePoint(position: Int) {
        this.referencePoints.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}