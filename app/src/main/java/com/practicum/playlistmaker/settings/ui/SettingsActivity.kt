package com.practicum.playlistmaker.settings.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.creator.AppCreator
import com.practicum.playlistmaker.R

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(AppCreator.provideSettingsInteractor())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsBackButton = findViewById<ImageView>(R.id.button_back_settings)
        val shareAppButton = findViewById<ImageView>(R.id.share_app_button)
        val writeToSupportButton = findViewById<ImageView>(R.id.write_to_support_button)
        val userAgreementButton = findViewById<ImageView>(R.id.user_agreement_button)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        viewModel.settingsState.observe(this) { state ->
            themeSwitcher.isChecked = state.isDarkTheme
        }

        themeSwitcher.isChecked = viewModel.settingsState.value?.isDarkTheme ?: false

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkTheme(isChecked)
        }

        settingsBackButton.setOnClickListener {
            finish()
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
            data = Uri.parse("mailto:")
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
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, viewModel.getEmailClientNotFoundMessage(), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openUserAgreement() {
        val agreementUrl = viewModel.getUserAgreementUrl()
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))

        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, viewModel.getBrowserNotFoundMessage(), Toast.LENGTH_SHORT).show()
        }
    }
}