package com.example.hkschoolfinder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.hkschoolfinder.databinding.ActivitySchoolDetailBinding
import java.util.Locale

class SchoolDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySchoolDetailBinding
    private var useTraditionalChinese = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySchoolDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val school = getSchoolFromIntent()
        if (school == null) {
            finish()
            return
        }

        useTraditionalChinese = intent.getBooleanExtra(EXTRA_USE_TC, false)

        binding.backButton.setOnClickListener {
            finish()
        }

        updateUILabels()
        showSchoolDetails(school)
    }

    private fun getSchoolFromIntent(): School? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_SCHOOL, School::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_SCHOOL) as? School
        }
    }

    private fun showSchoolDetails(school: School) {
        binding.titleText.text = school.displayName(useTraditionalChinese)
        binding.categoryText.text = listOf(school.displayCategory(useTraditionalChinese), school.level)
            .filter { it.isNotBlank() }
            .joinToString(" • ")

        val na = if (useTraditionalChinese) "未提供" else "Not available"
        binding.addressValue.text = school.displayAddress(useTraditionalChinese).ifBlank { na }
        binding.districtValue.text = school.displayDistrict(useTraditionalChinese).ifBlank { na }
        binding.financeValue.text = school.financeType.ifBlank { na }
        binding.genderValue.text = school.studentGender.ifBlank { na }
        binding.sessionValue.text = school.session.ifBlank { na }
        binding.phoneValue.text = school.telephone.ifBlank { na }
        binding.religionValue.text = school.religion.ifBlank { na }
        binding.websiteValue.text = school.website.ifBlank { na }

        binding.websiteButton.isEnabled = school.website.isNotBlank()
        binding.mapButton.isEnabled = school.hasLocation()

        binding.websiteButton.setOnClickListener {
            if (school.website.isBlank()) {
                Toast.makeText(this, "No website provided", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val website = normalizeWebsiteUrl(school.website)
            val intent = Intent(Intent.ACTION_VIEW, website.toUri())
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.open_website)))
            } catch (_: Exception) {
                Toast.makeText(this, getString(R.string.no_app_for_website), Toast.LENGTH_SHORT).show()
            }
        }

        binding.mapButton.setOnClickListener {
            if (!school.hasLocation()) {
                Toast.makeText(this, "No map location provided", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lat = school.latitude ?: return@setOnClickListener
            val lng = school.longitude ?: return@setOnClickListener
            val geoUri = "geo:$lat,$lng?q=$lat,$lng"
            val mapsWebUrl = String.format(
                Locale.US,
                "https://www.google.com/maps/search/?api=1&query=%f,%f",
                lat,
                lng
            )

            val geoIntent = Intent(Intent.ACTION_VIEW, geoUri.toUri())
            val webIntent = Intent(Intent.ACTION_VIEW, mapsWebUrl.toUri())

            try {
                startActivity(Intent.createChooser(geoIntent, getString(R.string.open_map)))
            } catch (_: Exception) {
                try {
                    startActivity(webIntent)
                } catch (_: Exception) {
                    Toast.makeText(this, getString(R.string.no_app_for_map), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun normalizeWebsiteUrl(value: String): String {
        val trimmed = value.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        return "https://$trimmed"
    }

    private fun updateUILabels() {
        binding.backButton.text = getString(if (useTraditionalChinese) R.string.back_tc else R.string.back)
        binding.addressLabel.text = getString(if (useTraditionalChinese) R.string.address_tc else R.string.address)
        binding.districtLabel.text = getString(if (useTraditionalChinese) R.string.district_tc else R.string.district)
        binding.financeLabel.text = getString(if (useTraditionalChinese) R.string.finance_type_tc else R.string.finance_type)
        binding.genderLabel.text = getString(if (useTraditionalChinese) R.string.students_gender_tc else R.string.students_gender)
        binding.sessionLabel.text = getString(if (useTraditionalChinese) R.string.session_tc else R.string.session)
        binding.phoneLabel.text = getString(if (useTraditionalChinese) R.string.telephone_tc else R.string.telephone)
        binding.religionLabel.text = getString(if (useTraditionalChinese) R.string.religion_tc else R.string.religion)
        binding.websiteLabel.text = getString(if (useTraditionalChinese) R.string.website_tc else R.string.website)
        binding.websiteButton.text = getString(if (useTraditionalChinese) R.string.open_school_website_tc else R.string.open_school_website)
        binding.mapButton.text = getString(if (useTraditionalChinese) R.string.open_in_map_tc else R.string.open_in_map)
    }

    companion object {
        private const val EXTRA_SCHOOL = "extra_school"
        private const val EXTRA_USE_TC = "extra_use_tc"

        fun newIntent(context: android.content.Context, school: School, useTraditionalChinese: Boolean): Intent {
            return Intent(context, SchoolDetailActivity::class.java).apply {
                putExtra(EXTRA_SCHOOL, school)
                putExtra(EXTRA_USE_TC, useTraditionalChinese)
            }
        }
    }
}
