package com.practicum.playlistmaker.settings.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.R
import org.koin.android.ext.android.get
import androidx.core.net.toUri

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by lazy { get() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shareAppButton = view.findViewById<ImageView>(R.id.share_app_button)
        val writeToSupportButton = view.findViewById<ImageView>(R.id.write_to_support_button)
        val userAgreementButton = view.findViewById<ImageView>(R.id.user_agreement_button)
        val themeSwitcher = view.findViewById<SwitchMaterial>(R.id.themeSwitcher)

        viewModel.settingsState.observe(viewLifecycleOwner) { state ->
            themeSwitcher.isChecked = state.isDarkTheme
        }

        themeSwitcher.isChecked = viewModel.settingsState.value?.isDarkTheme ?: false

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkTheme(isChecked)
        }

        shareAppButton.setOnClickListener {
            shareApp()
        }
        writeToSupportButton.setOnClickListener {
            writeToSupport()
        }

        userAgreementButton.setOnClickListener {
            openUserAgreement()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, viewModel.getShareMessage())
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun writeToSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(viewModel.getSupportEmail()))
            putExtra(Intent.EXTRA_SUBJECT, viewModel.getEmailSubject())
            putExtra(Intent.EXTRA_TEXT, viewModel.getEmailBody())
        }

        try {
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    viewModel.getEmailChooserTitle()
                )
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), viewModel.getEmailClientNotFoundMessage(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openUserAgreement() {
        val agreementUrl = viewModel.getUserAgreementUrl()
        val browserIntent = Intent(Intent.ACTION_VIEW, agreementUrl.toUri())

        try {
            startActivity(browserIntent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), viewModel.getBrowserNotFoundMessage(), Toast.LENGTH_SHORT).show()
        }
    }
}