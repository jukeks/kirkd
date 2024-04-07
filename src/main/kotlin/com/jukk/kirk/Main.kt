package com.jukk.kirk

import kotlinx.coroutines.runBlocking
import com.jukk.kirk.server.Server


fun main() {
    runBlocking {
        val server = Server("127.0.0.1", 9002)
        server.start(this)
    }
}