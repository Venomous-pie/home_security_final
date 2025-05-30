package com.example.smarthomeappfinal.ui.account

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smarthomeappfinal.LoginActivity
import com.example.smarthomeappfinal.R
import com.example.smarthomeappfinal.databinding.FragmentAccountBinding
import com.google.firebase.auth.EmailAuthProvider
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

        binding.itemAccountPassword.setOnClickListener {
            showChangePasswordDialog()
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

    private fun showChangePasswordDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 0)
        }

        // Create EditTexts for password input
        val currentPasswordInput = EditText(context).apply {
            hint = getString(R.string.current_password_label)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val newPasswordInput = EditText(context).apply {
            hint = getString(R.string.new_password_label)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val confirmNewPasswordInput = EditText(context).apply {
            hint = getString(R.string.confirm_new_password_label)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        // Add EditTexts to layout
        layout.addView(currentPasswordInput)
        layout.addView(newPasswordInput)
        layout.addView(confirmNewPasswordInput)

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.change_password_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.save_action)) { dialog, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmNewPassword = confirmNewPasswordInput.text.toString()

                if (validatePasswordInputs(newPassword, confirmNewPassword)) {
                    changePassword(currentPassword, newPassword)
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun validatePasswordInputs(newPassword: String, confirmNewPassword: String): Boolean {
        if (newPassword.length < 6) {
            Toast.makeText(context, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
            return false
        }

        if (newPassword != confirmNewPassword) {
            Toast.makeText(context, getString(R.string.passwords_dont_match), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        currentUser?.let { user ->
            // Re-authenticate user before changing password
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Re-authentication successful, now change password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, getString(R.string.password_changed_success), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "${getString(R.string.password_change_failed)}: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, getString(R.string.current_password_incorrect), Toast.LENGTH_SHORT).show()
                }
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