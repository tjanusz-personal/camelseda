package com.example.camelseda 

import org.slf4j.MDC

data class Response<out T>(val id: String, val status: Status, val value: T?, val failureMessage: String?, val logFile: String? = null) {
}

fun <T> success(value: T, id: String): Response<T> = Response(id = id, status = Status.OK, value = value, failureMessage = null, logFile = null)
fun <T> failure(failureMessage: String, id: String, logFile: String? = "") = Response<T>(id = id, status = Status.FAILURE, value = null, failureMessage = failureMessage, logFile = logFile)

enum class Status(val exitCode: Int) {
  OK(0),
  FAILURE(1);

  /**
   * If this object is equal to {@link #OK} then return true
   */
  fun isOK(): Boolean {
    return this == OK
  }

  /**
   * If this object is equal to {@link #FAILURE} then return true
   */
  fun isFailure(): Boolean {
    return this == FAILURE
  }
}