package net.maew.quicklibs

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import java.util.stream.Collectors

object QJsonUtils {
    @JvmStatic
    inline fun <reified T> getJsonObject(jsonString: String?): T {
        val jsonElement = Json.parseToJsonElement(jsonString!!)
        return Json.decodeFromJsonElement(jsonElement)
    }

    @JvmStatic
    inline fun <reified T> getJsonArray(jsonString: String?): List<T> {
        val jsonElement = Json.parseToJsonElement(jsonString!!)
        return (jsonElement as JsonArray).stream().map { e ->
            Json.decodeFromJsonElement<T>(e)
        }.collect(Collectors.toList())
    }

    @JvmStatic
    fun readResource(resourceFileName: String): String? {
        return {}.javaClass.getResource(resourceFileName)?.readText()
    }
}
