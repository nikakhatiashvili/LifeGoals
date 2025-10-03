package com.example.lifegoals.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
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