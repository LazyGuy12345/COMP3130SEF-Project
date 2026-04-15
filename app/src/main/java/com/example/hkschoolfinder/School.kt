package com.example.hkschoolfinder

import java.io.Serializable

data class School(
    val schoolNo: String,
    val nameEn: String,
    val nameTc: String,
    val categoryEn: String,
    val categoryTc: String,
    val addressEn: String,
    val addressTc: String,
    val districtEn: String,
    val districtTc: String,
    val financeType: String,
    val level: String,
    val studentGender: String,
    val session: String,
    val telephone: String,
    val faxNumber: String,
    val website: String,
    val religion: String,
    val longitude: Double?,
    val latitude: Double?
) : Serializable {
    fun hasLocation(): Boolean = latitude != null && longitude != null

    fun displayName(useTraditionalChinese: Boolean): String {
        if (useTraditionalChinese && nameTc.isNotBlank()) return nameTc
        return nameEn
    }

    fun displayCategory(useTraditionalChinese: Boolean): String {
        if (useTraditionalChinese && categoryTc.isNotBlank()) return categoryTc
        return categoryEn
    }

    fun displayAddress(useTraditionalChinese: Boolean): String {
        if (useTraditionalChinese && addressTc.isNotBlank()) return addressTc
        return addressEn
    }

    fun displayDistrict(useTraditionalChinese: Boolean): String {
        if (useTraditionalChinese && districtTc.isNotBlank()) return districtTc
        return districtEn
    }
}
