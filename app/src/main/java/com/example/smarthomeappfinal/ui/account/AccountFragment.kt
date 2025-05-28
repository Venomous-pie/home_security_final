package com.example.smarthomeappfinal.ui.account

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smarthomeappfinal.LoginActivity
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            // User is not logged in, navigate to LoginActivity
            navigateToLoginScreen()
            return root // Return early as further setup is not needed
        }

        populateUserDetails()

        binding.itemAccountName.setOnClickListener {
            showChangeUsernameDialog()
        }

        // Set up Password item click listener (placeholder for now)
        binding.itemAccountPassword.setOnClickListener {
            Toast.makeText(context, "Password - Create/Change clicked (Not Implemented)", Toast.LENGTH_SHORT).show()
        }

        // Set up Logout button click listener
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return root
    }

    private fun populateUserDetails() {
        currentUser?.let {
            binding.tvNameValue.text = it.displayName ?: extractNameFromEmail(it.email)
            binding.tvAccountEmailValue.text = it.email
        }
    }

    private fun extractNameFromEmail(email: String?): String {
        return email?.split("@")?.getOrNull(0) ?: getString(R.string.default_user_name)
    }

    private fun showChangeUsernameDialog() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            hint = getString(R.string.hint_new_username)
            setText(currentUser?.displayName)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_username_title))
            .setView(editText)
            .setPositiveButton(getString(R.string.save_action)) { dialog, _ ->
                val newUsername = editText.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    updateUsername(newUsername)
                } else {
                    Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateUsername(newUsername: String) {
        currentUser?.let {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername)
                .build()

            it.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, getString(R.string.username_updated_success), Toast.LENGTH_SHORT).show()
                        populateUserDetails() // Refresh UI
                    } else {
                        Toast.makeText(context, "${getString(R.string.username_update_failed)}: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } ?: run {
             Toast.makeText(context, "User not found. Please log in again.", Toast.LENGTH_SHORT).show()
             navigateToLoginScreen()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_logout_title))
            .setMessage(getString(R.string.confirm_logout_message))
            .setPositiveButton(getString(R.string.yes_action)) { dialog, _ ->
                logoutUser()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no_action)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        navigateToLoginScreen()
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 