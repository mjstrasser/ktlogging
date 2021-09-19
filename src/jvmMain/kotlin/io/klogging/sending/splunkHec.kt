/*

   Copyright 2021 Michael Strasser.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package io.klogging.sending

import io.klogging.events.LogEvent
import io.klogging.internal.trace
import io.klogging.internal.warn
import io.klogging.rendering.evalTemplate
import io.klogging.rendering.serializeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Send events to a Splunk HEC endpoint.
 *
 * @param endpoint HEC endpoint definition
 *
 * @return a [SendString] suspend function that sends each event string in a separate
 *         coroutine using the IO coroutine dispatcher.
 */
public actual fun splunkHec(endpoint: SplunkEndpoint): EventSender = { event ->
    coroutineScope {
        launch(Dispatchers.IO) {
            sendToSplunk(endpoint, event)
        }
    }
}

private fun sendToSplunk(endpoint: SplunkEndpoint, event: LogEvent) {
    val conn = hecConnection(endpoint)
    try {
        trace("Sending event in context ${Thread.currentThread().name}")
        conn.outputStream.use { it.write(splunkEvent(endpoint, event).toByteArray()) }
        val response = conn.inputStream.use { String(it.readAllBytes()) }
        if (conn.responseCode >= 400)
            warn("Error response ${conn.responseCode} sending event to Splunk: $response")
    } catch (e: IOException) {
        warn("Exception sending message to Splunk: $e")
    }
}

private const val TIME_MARKER = "XXX--TIME-MARKER--XXX"

private val Instant.decimalSeconds
    get() = "%d.%09d".format(epochSeconds, nanosecondsOfSecond)

/**
 * Convert a [LogEvent] to a JSON-formatted string for Splunk
 */
public fun splunkEvent(endpoint: SplunkEndpoint, event: LogEvent): String {
    val eventMap: Map<String, Any?> = (
        mapOf(
            "logger" to event.logger,
            "level" to event.level.name,
            "context" to event.context,
            "stackTrace" to event.stackTrace,
            "message" to event.evalTemplate()
        ) + event.items
        ).filterValues { it != null }
    val splunkMap: MutableMap<String, Any?> = mutableMapOf(
        "time" to TIME_MARKER, // Replace later
        "index" to endpoint.index,
        "sourcetype" to endpoint.sourceType,
        "host" to event.host,
        "event" to eventMap,
    )
    return serializeMap(splunkMap)
        .replace(""""$TIME_MARKER"""", event.timestamp.decimalSeconds)
}

private fun hecConnection(endpoint: SplunkEndpoint): HttpsURLConnection {
    val conn =
        URL("${endpoint.hecUrl}/services/collector/event").openConnection() as HttpsURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("Authorization", "Splunk ${endpoint.hecToken}")
    conn.doOutput = true
    return conn
}
