package com.jukk.kirkd

import kotlinx.coroutines.runBlocking
import com.jukk.kirkd.server.Server


fun main() {
    runBlocking {
        val server = Server("127.0.0.1", 9002)
        server.start(this)
    }
}