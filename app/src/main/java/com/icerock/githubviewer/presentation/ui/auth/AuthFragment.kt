package com.icerock.githubviewer.presentation.ui.auth

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.icerock.githubviewer.R
import com.icerock.githubviewer.databinding.FragmentAuthBinding
import com.icerock.githubviewer.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()

        observeViewModel()
    }

    private fun setupListeners() {
        binding.etTokenInput.addTextChangedListener { text ->
            authViewModel.onTextChanged(text.toString())
            updateTextInputLayoutColor(text.toString(), authViewModel.state.value)
        }

        binding.btnSignIn.setOnClickListener {
            hideKeyboard()
            authViewModel.onSignButtonPressed()
        }
    }

    private fun observeViewModel() {
        authViewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthViewModel.State.Idle -> showIdleState()
                is AuthViewModel.State.Loading -> showLoadingState()
                is AuthViewModel.State.InvalidInput -> showInvalidInputState(state.reason)
            }
            updateTextInputLayoutColor(binding.etTokenInput.text.toString(), state)
        }

        authViewModel.token.observe(viewLifecycleOwner) { token ->
            if (binding.etTokenInput.text.toString() != token) {
                binding.etTokenInput.setText(token)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.actions.collectLatest { action ->
                when (action) {
                    is AuthViewModel.Action.ShowError -> showErrorDialog(action.messageResId)
                    is AuthViewModel.Action.NavigateToRepositoriesList -> navigateToRepositoriesList()
                }
            }
        }
    }

    private fun showIdleState() {
        binding.btnSignIn.isEnabled = true
        binding.btnSignIn.text = getString(R.string.sign_in)
        binding.btnSignIn.icon = null
        binding.tvErrorMessage.visibility = View.GONE
    }

    private fun showLoadingState() {
        binding.btnSignIn.isEnabled = false
        binding.btnSignIn.text = getString(R.string.empty_text)
        binding.btnSignIn.icon = CircularProgressDrawable(requireContext()).apply {
            setStyle(CircularProgressDrawable.DEFAULT)
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.white))
            start()
        }
        binding.tvErrorMessage.visibility = View.GONE
    }

    private fun showInvalidInputState(reasonResId: Int) {
        binding.btnSignIn.isEnabled = true
        binding.btnSignIn.text = getString(R.string.sign_in)
        binding.btnSignIn.icon = null
        binding.tvErrorMessage.text = getString(reasonResId)
        binding.tvErrorMessage.visibility = View.VISIBLE
    }

    private fun showErrorDialog(messageResId: Int) {
        val errorMessage = getString(messageResId)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_common, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val titleTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val descriptionTextView = dialogView.findViewById<TextView>(R.id.tv_dialog_description)
        val positiveButton = dialogView.findViewById<Button>(R.id.btn_dialog_positive)
        val negativeButton = dialogView.findViewById<Button>(R.id.btn_dialog_negative)

        titleTextView.text = getString(R.string.dialog_error_title)
        descriptionTextView.text = errorMessage

        negativeButton.visibility = View.INVISIBLE

        positiveButton.text = getString(R.string.dialog_error_btn)
        positiveButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun navigateToRepositoriesList() {
        findNavController().navigate(R.id.action_auth_to_repositories_list)
    }

    private fun updateTextInputLayoutColor(text: String, state: AuthViewModel.State?) {
        val colorResId = when {
            state is AuthViewModel.State.Loading || text.isEmpty() -> R.color.grey
            state is AuthViewModel.State.InvalidInput -> R.color.error
            else -> R.color.secondary
        }
        val color = ContextCompat.getColor(requireContext(), colorResId)
        binding.tilTokenInput.boxStrokeColor = color
        binding.tilTokenInput.defaultHintTextColor = ColorStateList.valueOf(color)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
