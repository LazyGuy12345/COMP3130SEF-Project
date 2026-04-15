package com.example.hkschoolfinder

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

object SchoolRepository {
    private const val LIVE_URL =
        "https://www.edb.gov.hk/attachment/en/student-parents/sch-info/sch-search/sch-location-info/SCH_LOC_EDB.json"

    private val client = OkHttpClient()

    suspend fun loadSchools(context: Context): List<School> = withContext(Dispatchers.IO) {
        try {
            val onlineJson = downloadJson()
            val liveSchools = parseSchools(onlineJson)
            if (liveSchools.isNotEmpty()) {
                liveSchools
            } else {
                parseSchools(readAssetFile(context))
            }
        } catch (error: Exception) {
            parseSchools(readAssetFile(context))
        }
    }

    private fun downloadJson(): String {
        val request = Request.Builder()
            .url(LIVE_URL)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("Request failed: ${response.code}")
            }
            return response.body?.string().orEmpty()
        }
    }

    private fun readAssetFile(context: Context): String {
        return context.assets.open("schools_sample.json").bufferedReader().use { it.readText() }
    }

    private fun parseSchools(jsonText: String): List<School> {
        val jsonArray = JSONArray(jsonText)
        val schools = mutableListOf<School>()

        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(index) ?: continue

            val school = School(
                schoolNo = item.pickString("SCHOOL NO."),
                nameEn = item.pickString("ENGLISH NAME"),
                nameTc = item.pickString("中文名稱"),
                categoryEn = item.pickString("ENGLISH CATEGORY"),
                categoryTc = item.pickString("中文類別"),
                addressEn = item.pickString("ENGLISH ADDRESS"),
                addressTc = item.pickString("中文地址"),
                districtEn = item.pickString("DISTRICT"),
                districtTc = item.pickString("地區"),
                financeType = item.pickString("FINANCE TYPE"),
                level = item.pickString("SCHOOL LEVEL"),
                studentGender = item.pickString("STUDENTS GENDER"),
                session = item.pickString("SESSION"),
                telephone = item.pickString("TELEPHONE"),
                faxNumber = item.pickString("FAX NUMBER"),
                website = item.findWebsite(),
                religion = item.pickString("RELIGION"),
                longitude = item.pickDouble("LONGITUDE"),
                latitude = item.pickDouble("LATITUDE")
            )

            if (school.nameEn.isNotBlank() || school.nameTc.isNotBlank()) {
                schools.add(school)
            }
        }

        return schools.sortedWith(compareBy({ it.districtEn }, { it.nameEn }))
    }

    private fun JSONObject.pickString(vararg names: String): String {
        for (name in names) {
            val value = optString(name).trim()
            if (value.isNotBlank() && value != "null") {
                return value
            }
        }
        return ""
    }

    private fun JSONObject.pickDouble(vararg names: String): Double? {
        for (name in names) {
            if (has(name) && !isNull(name)) {
                val value = optDouble(name, Double.NaN)
                if (!value.isNaN()) {
                    return value
                }
            }
        }
        return null
    }

    private fun JSONObject.findWebsite(): String {
        val keys = keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = optString(key).trim()
            if (value.startsWith("http://") || value.startsWith("https://")) {
                return value
            }
            val upperKey = key.uppercase()
            if ((upperKey.contains("WEB") || upperKey.contains("SITE") || upperKey.contains("URL")) && value.isNotBlank()) {
                return value
            }
        }
        return ""
    }
}
