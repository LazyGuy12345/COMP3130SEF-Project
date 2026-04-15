package com.example.hkschoolfinder

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hkschoolfinder.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var schoolAdapter: SchoolAdapter
    private var allSchools: List<School> = emptyList()
    private var useTraditionalChinese = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            schoolAdapter = SchoolAdapter { school ->
                startActivity(SchoolDetailActivity.newIntent(this, school, useTraditionalChinese))
            }

            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = schoolAdapter

            binding.searchEditText.doAfterTextChanged {
                applyFilters()
            }

            binding.refreshButton.setOnClickListener {
                loadSchools()
            }

            binding.languageButton.setOnClickListener {
                useTraditionalChinese = !useTraditionalChinese
                updateLanguageButtonText()
                updateUILabels()
                schoolAdapter.setTraditionalChineseMode(useTraditionalChinese)
                setupSpinners(allSchools)
                applyFilters()
            }

            binding.swipeRefresh.setOnRefreshListener {
                loadSchools()
            }

            updateLanguageButtonText()
            loadSchools()
        } catch (error: Throwable) {
            Log.e("MainActivity", "Startup crash", error)
            val fallback = TextView(this).apply {
                text = "Startup error: ${error.message}"
                textSize = 16f
                setPadding(24, 24, 24, 24)
            }
            setContentView(fallback)
            Toast.makeText(this, "Startup error logged", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadSchools() {
        binding.progressBar.isVisible = true
        binding.refreshButton.isEnabled = false
        binding.statusText.text = getString(
            if (useTraditionalChinese) R.string.loading_message_tc else R.string.loading_message
        )

        lifecycleScope.launch {
            try {
                val schools = SchoolRepository.loadSchools(this@MainActivity)
                allSchools = schools
                schoolAdapter.setTraditionalChineseMode(useTraditionalChinese)
                setupSpinners(schools)
                applyFilters()
                Toast.makeText(
                    this@MainActivity,
                    "Loaded ${schools.size} schools",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (error: Exception) {
                binding.statusText.text = getString(
                    if (useTraditionalChinese) R.string.load_failed_message_tc else R.string.load_failed_message
                )
                Toast.makeText(
                    this@MainActivity,
                    "Cannot load the school list right now.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.isVisible = false
                binding.refreshButton.isEnabled = true
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun setupSpinners(schools: List<School>) {
        val allDistrictsLabel = getString(
            if (useTraditionalChinese) R.string.all_districts_tc else R.string.all_districts_en
        )

        val districts = listOf(allDistrictsLabel) + schools.map { it.displayDistrict(useTraditionalChinese) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        val allLevelsLabel = getString(
            if (useTraditionalChinese) R.string.all_levels_tc else R.string.all_levels_en
        )

        val levels = listOf(allLevelsLabel) + schools.map { it.level }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()

        binding.districtSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            districts
        )

        binding.levelSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            levels
        )

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.districtSpinner.onItemSelectedListener = listener
        binding.levelSpinner.onItemSelectedListener = listener
    }

    private fun applyFilters() {
        val keyword = binding.searchEditText.text?.toString().orEmpty().trim().lowercase()
        val selectedDistrict = binding.districtSpinner.selectedItem?.toString().orEmpty()
        val selectedLevel = binding.levelSpinner.selectedItem?.toString().orEmpty()
        val allDistrictsLabel = getString(
            if (useTraditionalChinese) R.string.all_districts_tc else R.string.all_districts_en
        )
        val allLevelsLabel = getString(
            if (useTraditionalChinese) R.string.all_levels_tc else R.string.all_levels_en
        )

        val filteredSchools = allSchools.filter { school ->
            val matchesKeyword = if (keyword.isBlank()) {
                true
            } else {
                listOf(
                    school.nameEn,
                    school.nameTc,
                    school.addressEn,
                    school.addressTc,
                    school.districtEn,
                    school.districtTc,
                    school.categoryEn,
                    school.categoryTc
                )
                    .joinToString(" ")
                    .lowercase()
                    .contains(keyword)
            }

            val matchesDistrict = selectedDistrict.isBlank() ||
                selectedDistrict == allDistrictsLabel ||
                school.displayDistrict(useTraditionalChinese).equals(selectedDistrict, ignoreCase = true)

            val matchesLevel = selectedLevel.isBlank() ||
                selectedLevel == allLevelsLabel ||
                school.level.equals(selectedLevel, ignoreCase = true)

            matchesKeyword && matchesDistrict && matchesLevel
        }

        schoolAdapter.updateSchools(filteredSchools)
        binding.statusText.text = getString(
            if (useTraditionalChinese) R.string.result_summary_tc else R.string.result_summary,
            filteredSchools.size,
            allSchools.size
        )
    }

    private fun updateLanguageButtonText() {
        binding.languageButton.text = getString(
            if (useTraditionalChinese) R.string.switch_to_english else R.string.switch_to_traditional_chinese
        )
    }

    private fun updateUILabels() {
        binding.titleText.text = getString(
            if (useTraditionalChinese) R.string.app_name_tc else R.string.app_name
        )
        binding.subtitleText.text = getString(
            if (useTraditionalChinese) R.string.home_subtitle_tc else R.string.home_subtitle
        )
        binding.searchInputLayout.hint = getString(
            if (useTraditionalChinese) R.string.search_hint_tc else R.string.search_hint
        )
        binding.refreshButton.text = getString(
            if (useTraditionalChinese) R.string.refresh_school_list_tc else R.string.refresh_school_list
        )
    }
}
