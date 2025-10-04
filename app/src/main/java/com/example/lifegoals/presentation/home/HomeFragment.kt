package com.example.lifegoals.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifegoals.data.model.UiStats
import com.example.lifegoals.databinding.FragmentHomeBinding
import com.example.lifegoals.presentation.home.adapter.GoalsAdapter
import com.example.lifegoals.presentation.home.dialog.AddGoalDialogFragment
import com.example.lifegoals.presentation.launchAndRepeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var goalsAdapter: GoalsAdapter

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
        binding.fabAddGoal.setOnClickListener {
            AddGoalDialogFragment { goal ->
                viewModel.addGoal(goal)
            }.show(parentFragmentManager, "AddGoalDialog")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeData() {
        launchAndRepeatOnLifecycle {
            viewModel.allGoals.collect { goals ->
                Log.wtf("asdasdas", goals.size.toString())
                goalsAdapter.submitList(goals)
            }
        }
        launchAndRepeatOnLifecycle {
            viewModel.uiStats.collect {
                showStats(it)
            }
        }
    }

    private fun showStats(stats: UiStats) {
        binding.statsCardLayout.tvPointsToday.text = "Points Today: ${stats.pointsToday}"
        binding.statsCardLayout.tvTotalPoints.text = "Total Points: ${stats.totalPoints}"
        binding.statsCardLayout.tvGoalsCompletedToday.text = "Goals Completed Today: ${stats.goalsCompletedToday}"
        binding.statsCardLayout.tvWeeklyRate.text =
            "Weekly Completion Rate: ${(stats.weeklyCompletionRate ?: 0f) * 100}%"
        binding.statsCardLayout.tvMilestones.text =
            "Milestones: ${stats.milestonesCompleted} out of ${stats.milestonesTotal}"
        binding.statsCardLayout.tvMilestonePoints.text =
            "Milestone Points: ${stats.milestonePointsCompleted} out of ${stats.milestonePointsTotal}"
        binding.statsCardLayout.tvCurrentStreak.text =
            "Current Streak: ${stats.currentStreak}"
        binding.statsCardLayout.tvBestStreak.text =
            "(Best Streak: ${stats.bestStreak})"
    }

    private fun setupViews() {
        goalsAdapter = GoalsAdapter { action ->
            viewModel.handleAction(action)
        }
        binding.rvGoals.apply {
            adapter = goalsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }
}