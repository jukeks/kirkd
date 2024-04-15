package com.jukk.kirkd

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.jukk.kirkd.server.Server


fun main() {
    runBlocking {
        withContext(Dispatchers.Default) {
            val server = Server("127.0.0.1", 9002)
            server.start(this)
        }
    }
}