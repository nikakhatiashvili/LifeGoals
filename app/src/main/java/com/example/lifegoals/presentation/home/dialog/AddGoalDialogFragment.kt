package com.example.lifegoals.presentation.home.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.lifegoals.data.model.GoalEntity
import com.example.lifegoals.data.model.GoalType
import com.example.lifegoals.databinding.DialogAddGoalBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddGoalDialogFragment(
    private val onGoalAdded: (GoalEntity) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddGoalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddGoalBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("Add Goal")
            .setPositiveButton("Save") { _, _ ->
                val name = binding.etName.text.toString()
                val description = binding.etDescription.text.toString()
                val points = binding.etPoints.text.toString().toIntOrNull() ?: 0
                val type =
                    if (binding.rbDaily.isChecked) GoalType.DAILY else GoalType.MILESTONE

                val goal = GoalEntity(
                    name = name,
                    description = description,
                    pointsReward = points,
                    type = type
                )
                onGoalAdded(goal)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}