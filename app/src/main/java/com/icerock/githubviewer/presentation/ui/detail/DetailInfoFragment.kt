package com.icerock.githubviewer.presentation.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.icerock.githubviewer.R
import com.icerock.githubviewer.databinding.FragmentDetailInfoBinding
import com.icerock.githubviewer.domain.entity.RepoDetails
import com.icerock.githubviewer.presentation.viewmodel.RepositoryInfoViewModel
import com.icerock.githubviewer.util.ErrorHandler.ErrorType
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailInfoFragment : Fragment() {
    private var _binding: FragmentDetailInfoBinding? = null
    private val binding get() = _binding!!

    private val repositoryInfoViewModel: RepositoryInfoViewModel by viewModels()
    private val args: DetailInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repoId = args.repoId
        setupToolbar()
        repositoryInfoViewModel.loadRepositoryDetails(repoId)
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbarDetailInfo.setNavigationOnClickListener {
            repositoryInfoViewModel.back()
        }

        binding.toolbarDetailInfo.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    repositoryInfoViewModel.logout()
                    true
                }

                else -> false
            }
        }
    }

    private fun observeViewModel() {
        repositoryInfoViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RepositoryInfoViewModel.State.Loading -> showLoading()
                is RepositoryInfoViewModel.State.Loaded -> showRepoDetails(
                    state.githubRepo,
                    state.readmeState
                )

                is RepositoryInfoViewModel.State.Error -> {
                    binding.svDetailInfo.visibility = View.GONE
                    showError(
                        state.errorType,
                        state.errorMessageResId
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repositoryInfoViewModel.actions.collectLatest { action ->
                when (action) {
                    is RepositoryInfoViewModel.Action.NavigateToRepositoriesList -> {
                        findNavController().navigate(R.id.action_detail_info_to_repositories_list)
                    }

                    is RepositoryInfoViewModel.Action.NavigateToAuth -> {
                        findNavController().navigate(R.id.action_detail_info_to_auth)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.cpiLoadingDetailInfo.visibility = View.VISIBLE
        binding.svDetailInfo.visibility = View.GONE
        binding.clErrorLayoutDetailInfo.visibility = View.GONE
    }

    private fun showRepoDetails(
        repoDetails: RepoDetails,
        readmeState: RepositoryInfoViewModel.ReadmeState
    ) {
        binding.cpiLoadingDetailInfo.visibility = View.GONE
        binding.svDetailInfo.visibility = View.VISIBLE
        binding.clErrorLayoutDetailInfo.visibility = View.GONE

        binding.toolbarDetailInfo.title = repoDetails.name

        binding.repositoryLink.text = repoDetails.htmlUrl
        binding.repositoryLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(repoDetails.htmlUrl)
            }
            startActivity(intent)
        }
        binding.repositoryLicense.text =
            repoDetails.license?.name ?: getString(R.string.error_no_license)
        binding.repositoryStarsLabel.text = repoDetails.stars.toString()
        binding.repositoryForksLabel.text = repoDetails.forks.toString()
        binding.repositoryWatchersLabel.text = repoDetails.watchers.toString()

        handleReadmeState(readmeState)
    }

    private fun handleReadmeState(readmeState: RepositoryInfoViewModel.ReadmeState) {
        when (readmeState) {
            is RepositoryInfoViewModel.ReadmeState.Loading -> showReadmeLoading()
            is RepositoryInfoViewModel.ReadmeState.Loaded -> displayMarkdown(decodeReadmeContent(readmeState.markdown))
            is RepositoryInfoViewModel.ReadmeState.Empty -> showEmptyReadme()
            is RepositoryInfoViewModel.ReadmeState.Error -> {
                binding.readmeContainer.visibility = View.GONE
                showError(readmeState.errorType, readmeState.errorMessageResId)
            }
        }
    }

    private fun showReadmeLoading() {
        binding.cpiReadmeLoadingDetailInfo.visibility = View.VISIBLE
        binding.readmeContainer.visibility = View.GONE
        binding.clErrorLayoutDetailInfo.visibility = View.GONE
    }

    private fun decodeReadmeContent(encodedContent: String): String {
        val decodedBytes = Base64.decode(encodedContent, Base64.DEFAULT)
        return String(decodedBytes, Charsets.UTF_8)
    }

    private fun displayMarkdown(markdown: String) {
        binding.cpiReadmeLoadingDetailInfo.visibility = View.GONE
        binding.readmeContainer.visibility = View.VISIBLE
        binding.clErrorLayoutDetailInfo.visibility = View.GONE
        val markwon = Markwon.create(requireContext())
        markwon.setMarkdown(binding.readmeText, markdown)
    }

    private fun showEmptyReadme() {
        binding.readmeContainer.visibility = View.GONE
        showError(ErrorType.EmptyError, R.string.error_empty_readme)
    }

    private fun showError(errorType: ErrorType, errorMessageResId: Int) {
        binding.cpiLoadingDetailInfo.visibility = View.GONE
        binding.cpiReadmeLoadingDetailInfo.visibility = View.GONE
        binding.clErrorLayoutDetailInfo.visibility = View.VISIBLE

        binding.tvErrorTitleDetailInfo.setText(errorMessageResId)
        binding.ivErrorImageDetailInfo.contentDescription =
            binding.tvErrorTitleDetailInfo.text

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
                R.string.empty_text,
                R.string.refresh
            )

            else -> configureErrorDisplay(
                R.drawable.ic_unknown_error,
                R.color.error,
                R.string.error_unknown_description,
                R.string.retry
            )
        }

        binding.btnErrorDetailInfo.setOnClickListener {
            repositoryInfoViewModel.loadRepositoryDetails(args.repoId)
        }
    }

    private fun configureErrorDisplay(
        imageResId: Int,
        textColorResId: Int,
        descriptionResId: Int,
        buttonTextResId: Int
    ) {
        binding.ivErrorImageDetailInfo.setImageResource(imageResId)
        binding.tvErrorTitleDetailInfo.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                textColorResId
            )
        )
        binding.tvErrorDescriptionDetailInfo.text = getString(descriptionResId)
        binding.btnErrorDetailInfo.text = getString(buttonTextResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}