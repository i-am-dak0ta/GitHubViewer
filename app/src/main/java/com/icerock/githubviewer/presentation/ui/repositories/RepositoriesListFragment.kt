package com.icerock.githubviewer.presentation.ui.repositories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.icerock.githubviewer.R
import com.icerock.githubviewer.databinding.FragmentRepositoriesListBinding
import com.icerock.githubviewer.domain.entity.Repo
import com.icerock.githubviewer.presentation.viewmodel.RepositoriesListViewModel
import com.icerock.githubviewer.util.ErrorHandler.ErrorType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RepositoriesListFragment : Fragment() {

    private var _binding: FragmentRepositoriesListBinding? = null
    private val binding get() = _binding!!

    private val repositoriesListViewModel: RepositoriesListViewModel by viewModels()
    private lateinit var adapter: RepositoriesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRepositoriesListBinding.inflate(inflater, container, false)
        setupBackButtonHandler()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToolbarActions()

        repositoriesListViewModel.loadRepositories()

        observeViewModel()
    }

    private fun setupBackButtonHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_common, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val positiveButton = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_positive)
            val negativeButton = dialogView.findViewById<MaterialButton>(R.id.btn_dialog_negative)
            val titleTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
            val descriptionTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_description)

            titleTextView.text = getString(R.string.dialog_exit_title)
            descriptionTextView.text = getString(R.string.dialog_exit_description)
            positiveButton.text = getString(R.string.dialog_exit_positive_btn)
            negativeButton.text = getString(R.string.dialog_exit_negative_btn)

            positiveButton.setOnClickListener {
                dialog.dismiss()
                requireActivity().finish()
            }

            negativeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setupRecyclerView() {
        binding.rvRepositoriesList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupToolbarActions() {
        binding.toolbarRepositoriesList.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    repositoriesListViewModel.logout()
                    true
                }

                else -> false
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repositoriesListViewModel.state.collectLatest { state ->
                when (state) {
                    is RepositoriesListViewModel.State.Loading -> showLoading()
                    is RepositoriesListViewModel.State.Success -> showData(state.repos)
                    is RepositoriesListViewModel.State.Error -> showError(
                        state.errorType,
                        state.errorMessageResId
                    )

                    is RepositoriesListViewModel.State.Empty -> showEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repositoriesListViewModel.actions.collectLatest { action ->
                when (action) {
                    is RepositoriesListViewModel.Action.NavigateToAuth -> {
                        findNavController().navigate(R.id.action_repositories_list_to_auth)
                    }

                    is RepositoriesListViewModel.Action.NavigateToDetails -> {
                        val actionDetail = RepositoriesListFragmentDirections
                            .actionRepositoriesListToDetailInfo(action.repoId)
                        findNavController().navigate(actionDetail)
                    }
                }
            }
        }
    }

    private fun showData(repos: List<Repo>) {
        binding.clErrorLayoutRepositoriesList.visibility = View.GONE
        binding.cpiLoadingRepositoriesList.visibility = View.GONE
        binding.rvRepositoriesList.visibility = View.VISIBLE

        adapter = RepositoriesListAdapter(repos) { repo ->
            repositoriesListViewModel.onRepoSelected(repo)
        }
        binding.rvRepositoriesList.adapter = adapter
    }

    private fun showLoading() {
        binding.cpiLoadingRepositoriesList.visibility = View.VISIBLE
        binding.clErrorLayoutRepositoriesList.visibility = View.GONE
        binding.rvRepositoriesList.visibility = View.GONE
    }

    private fun showError(errorType: ErrorType, errorMessageResId: Int) {
        binding.cpiLoadingRepositoriesList.visibility = View.GONE
        binding.rvRepositoriesList.visibility = View.GONE
        binding.clErrorLayoutRepositoriesList.visibility = View.VISIBLE

        binding.tvErrorTitleRepositoriesList.setText(errorMessageResId)
        binding.ivErrorImageRepositoriesList.contentDescription =
            binding.tvErrorTitleRepositoriesList.text

        when (errorType) {
            is ErrorType.ConnectionError -> configureErrorDisplay(
                R.drawable.ic_network_error,
                R.color.error,
                R.string.error_network_description,
                R.string.retry
            )

            is ErrorType.HttpError -> configureErrorDisplay(
                R.drawable.ic_network_error,
                R.color.error,
                R.string.http_error_description,
                R.string.retry
            )

            is ErrorType.EmptyError -> configureErrorDisplay(
                R.drawable.ic_empty_error,
                R.color.secondary,
                R.string.error_empty_repositories_list_description,
                R.string.refresh
            )

            else -> configureErrorDisplay(
                R.drawable.ic_unknown_error,
                R.color.error,
                R.string.error_unknown_description,
                R.string.retry
            )
        }

        binding.btnErrorRepositoriesList.setOnClickListener {
            repositoriesListViewModel.loadRepositories()
        }
    }

    private fun configureErrorDisplay(
        imageResId: Int,
        textColorResId: Int,
        descriptionResId: Int,
        buttonTextResId: Int
    ) {
        binding.ivErrorImageRepositoriesList.setImageResource(imageResId)
        binding.tvErrorTitleRepositoriesList.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                textColorResId
            )
        )
        binding.tvErrorDescriptionRepositoriesList.text = getString(descriptionResId)
        binding.btnErrorRepositoriesList.text = getString(buttonTextResId)
    }

    private fun showEmpty() {
        showError(ErrorType.EmptyError, R.string.error_empty_repositories_list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
