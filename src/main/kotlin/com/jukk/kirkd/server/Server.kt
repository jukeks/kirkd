package com.jukk.kirkd.server


import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*

import com.jukk.kirkd.client.Client
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class Server(val hostname: String, val port: Int, private val alive: AtomicBoolean = AtomicBoolean(true)) {
    suspend fun start(scope: CoroutineScope) {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind(hostname, port)

        val state = State()
        val handler = Handler(hostname, state)

        scope.launch {
            handler.handlerLoop()
        }

        logger.info("Server started")
        while (alive.get()) {
            val socket = serverSocket.accept()

            logger.info("Accepted client from ${socket.remoteAddress}")

            val client = Client(socket, handler.commandChannel)
            scope.launch {
                client.handle()
            }
        }
    }
}