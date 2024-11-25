package com.nyaa.saizeriya

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream


data class Order_Element(
    var id: String,
    var count: Int
)
data class Preset_Element(
    var date: String,
    var items: List<Order_Element>
)

class dataManager(private val context: Context) {
    private val sharedpre: SharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveJsonData(key: String, data: Any) {
        val json = gson.toJson(data)
        sharedpre.edit().putString(key, json).apply()
    }
    fun <T> getJsonData(key: String, typeToken: TypeToken<T>): T? {
        val json = sharedpre.getString(key, null) ?: return null
        return gson.fromJson(json, typeToken.type)
    }
    fun clearJsonData() {
        sharedpre.edit().clear().apply()
    }

    fun loadJsonFromAssets(fileName: String, assets: AssetManager): String {
        val inputStream: InputStream = assets.open(fileName)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        return String(buffer, Charsets.UTF_8)
    }

}

fun JSONArray.findElementById(key: String, search: String): JSONObject? {
    for (i in 0 until this.length()) {
        if (this.getJSONObject(i).getString(key) == search) {
            return this.getJSONObject(i)
        }
    }
    return null
}
fun List<Order_Element>.findElementById(search: String): Order_Element? {
    for (i in 0 until this.size) {
        if (this[i].id == search) {
            return this[i]
        }
    }
    return null
}
fun List<Preset_Element>.findElementById(search: String): Preset_Element? {
    for (i in 0 until this.size) {
        if (this[i].date == search) {
            return this[i]
        }
    }
    return null
}
fun List<Order_Element>.getIndexById(search: String): Int {
    for (i in 0 until this.size) {
        if (this[i].id == search) {
            return i
        }
    }
    return -1
}
fun JSONArray.findAllElementById(key: String, search: String): JSONArray? {
    var jsonArray = JSONArray()
    for (i in 0 until this.length()) {
        if (this.getJSONObject(i).getString(key) == search) {
            jsonArray.put(this.getJSONObject(i))
        }
    }
    return jsonArray
}
fun JSONArray.getRandomElements(count: Int): JSONArray {
    val list = (0 until this.length()).map { this.get(it) }
    return JSONArray(list.shuffled().take(count))
}