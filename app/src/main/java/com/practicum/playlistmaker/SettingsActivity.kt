package com.practicum.playlistmaker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPref = getSharedPreferences(THEME_PREFS_KEY, MODE_PRIVATE)

        val settingsBackButton = findViewById<ImageView>(R.id.button_back_settings)
        val shareAppButton = findViewById<ImageView>(R.id.share_app_button)
        val writeToSupportButton = findViewById<ImageView>(R.id.write_to_support_button)
        val userAgreementButton = findViewById<ImageView>(R.id.user_agreement_button)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        val isDarkTheme = sharedPref.getBoolean(DARK_THEME_KEY, false)
        themeSwitcher.isChecked = isDarkTheme

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            switchTheme(isChecked)
            saveThemeState(isChecked)
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

    private fun switchTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    private fun saveThemeState(isDarkTheme: Boolean) {
        with(sharedPref.edit()) {
            putBoolean(DARK_THEME_KEY, isDarkTheme)
            apply()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun writeToSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body))
        }

        try {
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    getString(R.string.email_chooser_title)
                )
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "На устройстве не найден почтовый клиент", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openUserAgreement() {
        val agreementUrl = getString(R.string.user_agreement_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(agreementUrl))

        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "На устройстве не найден браузер", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val THEME_PREFS_KEY = "theme_prefs"
        const val DARK_THEME_KEY = "dark_theme"
    }
}
