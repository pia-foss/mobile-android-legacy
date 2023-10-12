package com.privateinternetaccess.android.core

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.privateinternetaccess.android.tests.SignInTests

object DataFactory {
    fun loadTestDataFromJson(jsonString : String): List<TestData> {
        val gson = Gson()
        val listType = object : TypeToken<List<TestData>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}

data class TestData(val username: String, val password: String)
