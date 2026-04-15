package com.example.hkschoolfinder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hkschoolfinder.databinding.ItemSchoolBinding

class SchoolAdapter(
    private val onSchoolClicked: (School) -> Unit
) : RecyclerView.Adapter<SchoolAdapter.SchoolViewHolder>() {

    private val schools = mutableListOf<School>()
    private var useTraditionalChinese = false

    fun setTraditionalChineseMode(enabled: Boolean) {
        useTraditionalChinese = enabled
        notifyDataSetChanged()
    }

    fun updateSchools(newSchools: List<School>) {
        schools.clear()
        schools.addAll(newSchools)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolViewHolder {
        val binding = ItemSchoolBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SchoolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SchoolViewHolder, position: Int) {
        holder.bind(schools[position])
    }

    override fun getItemCount(): Int = schools.size

    inner class SchoolViewHolder(
        private val binding: ItemSchoolBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(school: School) {
            binding.nameText.text = school.displayName(useTraditionalChinese)
            binding.categoryText.text = school.displayCategory(useTraditionalChinese).ifBlank { "School" }
            binding.infoText.text = listOf(school.displayDistrict(useTraditionalChinese), school.level, school.financeType)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
            binding.addressText.text = school.displayAddress(useTraditionalChinese).ifBlank { "Address not available" }

            binding.root.setOnClickListener {
                onSchoolClicked(school)
            }
        }
    }
}
