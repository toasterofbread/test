package dev.toastbits.lifelog.application.worker

import korlibs.io.lang.TextDecoder
import korlibs.io.lang.TextEncoder
import korlibs.wasm.jsArrayOf
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Uint8Array
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.MessageEvent
import org.w3c.dom.Worker

internal class WorkerEncoder {
    private val textDecoder: TextDecoder = TextDecoder(CHARSET)
    private val textEncoder: TextEncoder = TextEncoder(CHARSET)

    inline fun <reified T> decode(message: MessageEvent): T {
        val array: ArrayBuffer = message.data as ArrayBuffer
        val view: DataView = DataView(array, 0, array.byteLength)
        return workerJson.decodeFromString<T>(textDecoder.decode(view))
    }

    fun post(workerScope: DedicatedWorkerGlobalScope, data: String) {
        println("Posting data from worker (${data.length} chars)")
        println(data)
        val array: Uint8Array = textEncoder.encode(data)
        workerScope.postMessage(array.buffer, jsArrayOf(array.buffer))
    }

    fun post(worker: Worker, data: String) {
        println("Posting data to worker (${data.length} chars)")
        println(data)
        val array: Uint8Array = textEncoder.encode(data)
        worker.postMessage(array.buffer, jsArrayOf(array.buffer))
    }

    companion object {
        private const val CHARSET: String = "utf-8"
    }
}
