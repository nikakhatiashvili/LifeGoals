package com.example.lifegoals.presentation.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifegoals.data.model.GoalWithCompletion
import com.example.lifegoals.databinding.ItemGoalBinding
import com.example.lifegoals.presentation.hide
import com.example.lifegoals.presentation.home.actions.GoalActions
import com.example.lifegoals.presentation.show

class GoalsAdapter(
    private val onAction: (GoalActions) -> Unit
) : ListAdapter<GoalWithCompletion, GoalsAdapter.GoalViewHolder>(DiffCallback) {

    inner class GoalViewHolder(
        private val binding: ItemGoalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: GoalWithCompletion) {
            val goal = item.goal
            binding.tvGoalName.text = goal.name
            binding.tvGoalDescription.text = goal.description
            binding.tvGoalDescription.apply {
                if (goal.description.isNullOrEmpty()) hide() else show()
            }

            binding.tvPoints.text = "points: ${goal.pointsReward}"


            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = item.isCompleted
            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onAction(GoalActions.OnCheckBoxClick(item, isChecked))
            }

            binding.root.setOnClickListener {
                onAction(GoalActions.OnWholeItemClick(item))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object DiffCallback : DiffUtil.ItemCallback<GoalWithCompletion>() {
        override fun areItemsTheSame(oldItem: GoalWithCompletion, newItem: GoalWithCompletion): Boolean =
            oldItem.goal.id == newItem.goal.id

        override fun areContentsTheSame(oldItem: GoalWithCompletion, newItem: GoalWithCompletion): Boolean =
            oldItem == newItem
    }
}

