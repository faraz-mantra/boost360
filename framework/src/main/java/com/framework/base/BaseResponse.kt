package com.framework.base

import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.nio.charset.Charset

open class BaseResponse(
  var taskcode: Int? = null,
  var status: Int? = null,
  var message: String? = null,
  var error: Throwable? = null,
  var stringResponse: String? = null,
  var arrayResponse: Array<*>? = null,
  var anyResponse: Any? = null,
  var responseBody: ResponseBody? = null,
) : Serializable {

  //Deprecate
  fun message(): String {
    val message = message ?: "Something went wrong!"
    return try {
      val jsonObj = JSONObject(message)
      jsonObj.getString("Message") ?: jsonObj.getString("message") ?: message
    } catch (ex: Exception) {
      message
    }
  }

  //Deprecate
  fun errorMessage(): String? {
    val message = message ?: "Something went wrong!"
    return try {
      val jsonObj = JSONObject(message)
      val error = jsonObj.getJSONObject("Error")
      error.getString("ErrorDescription") ?: jsonObj.getString("errorDescription") ?: message
    } catch (ex: Exception) {
      message
    }
  }

  //Deprecate
  fun errorNMessage(): String? {
    val message = message ?: "Something went wrong!"
    return try {
      val jsonObj = JSONObject(message)
      val error = jsonObj.getJSONObject("Error").getJSONObject("ErrorList")
      return error.getString("EXCEPTION")
    } catch (ex: Exception) {
      message
    }
  }

  //Deprecate
  fun errorIPMessage(): String? {
    val message = message
    return try {
      val jsonObj = JSONObject(message)
      val error = jsonObj.getJSONObject("Error").getJSONObject("ErrorList")
      return error.getString("INVALID PARAMETERS")
    } catch (ex: Exception) {
      message
    }
  }

  //New Error message filter
  fun errorFlowMessage(): String? {
    val message = message
    return try {
      val jsonObj = JSONObject(message)
      val error: JSONArray? = jsonObj.getJSONArray("errors")
      val jsonResult = if (error?.length() ?: 0 >= 1) error?.get(0) as? JSONObject else null
      return jsonResult?.getString("message") ?: jsonObj.getString("Message") ?: jsonObj.getString("EXCEPTION") ?: messageN()
    } catch (ex: Exception) {
      messageN()
    }
  }

  fun messageN(): String? {
    val message = message
    return try {
      val jsonObj = JSONObject(message)
      jsonObj.getString("Message") ?: jsonObj.getString("message") ?: errorMessageN()
    } catch (ex: Exception) {
      errorMessageN()
    }
  }

  fun errorMessageN(): String? {
    val message = message
    return try {
      val jsonObj = JSONObject(message)
      val error: JSONObject? = jsonObj.getJSONObject("Error")
      error?.getString("ErrorDescription") ?: error?.getString("errorDescription") ?: errorNMessageN()
    } catch (ex: Exception) {
      errorNMessageN()
    }
  }

  fun errorNMessageN(): String? {
    val message = message
    return try {
      val jsonObj = JSONObject(message)
      val error: JSONObject? = jsonObj.getJSONObject("Error")?.getJSONObject("ErrorList")
      return error?.getString("EXCEPTION") ?: errorIPMessageN()
    } catch (ex: Exception) {
      errorIPMessageN()
    }
  }

  fun errorIPMessageN(): String? {
    val message = message
    return try {
      val jsonObj = JSONObject(message)
      val error: JSONObject? = jsonObj.getJSONObject("Error")?.getJSONObject("ErrorList")
      return error?.getString("INVALID PARAMETERS")
    } catch (ex: Exception) {
      message
    }
  }

  fun isSuccess(): Boolean {
    return status == 200 || status == 201 || status == 202 || status == 204
  }

  fun parseStringResponse(): String? {
    return try {
      val source: BufferedSource? = responseBody?.source()
      source?.request(Long.MAX_VALUE)
      val buffer: Buffer? = source?.buffer
      buffer?.clone()?.readString(Charset.forName("UTF-8"))
    } catch (e: Exception) {
      e.printStackTrace()
      ""
    }
  }

  fun parseResponse(): Boolean {
    return try {
      val source: BufferedSource? = responseBody?.source()
      source?.request(Long.MAX_VALUE)
      val buffer: Buffer? = source?.buffer
      val responseBodyString: String? = buffer?.clone()?.readString(Charset.forName("UTF-8"))
      responseBodyString.toBoolean()
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}