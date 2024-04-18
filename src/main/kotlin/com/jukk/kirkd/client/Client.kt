package com.jukk.kirkd.client

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import com.jukk.kirkd.protocol.Message
import com.jukk.kirkd.protocol.Parser
import com.jukk.kirkd.server.Command
import io.ktor.util.network.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import mu.KotlinLogging
import java.io.IOException
import java.net.SocketException
import java.util.concurrent.atomic.AtomicBoolean


private val logger = KotlinLogging.logger {}

class Client(
    private val socket: Socket?,
    private val commandChannel: Channel<Command>,
    outBufferLen: Int = 400
) {
    private val alive: AtomicBoolean = AtomicBoolean(true)
    private val readChannel: ByteReadChannel? = socket?.openReadChannel()
    private val writeChannel: ByteWriteChannel? = socket?.openWriteChannel()

    private val outQueue = Channel<String>(outBufferLen)

    private var nick: String = ""
    private var user: String = ""
    private var realName: String = ""
    private val hostname: String = socket?.remoteAddress?.toJavaAddress()?.hostname ?: ""
    private var registered = false
    private var channels = mutableSetOf<String>()

    fun isRegistered(): Boolean {
        return registered
    }

    fun setRegistered() {
        registered = true
    }

    fun hasAllInfo(): Boolean {
        return nick.isNotEmpty() && user.isNotEmpty() && realName.isNotEmpty()
    }

    fun getFullmask(): String {
        return "$nick!$user@$hostname"
    }

    fun setNick(nick: String) {
        this.nick = nick
    }

    fun getNick(): String {
        return nick
    }

    fun setUser(user: String) {
        this.user = user
    }

    fun getUser(): String {
        return user
    }

    fun setRealName(realName: String) {
        this.realName = realName
    }

    fun getRealName(): String {
        return realName
    }

    fun addChannel(channel: String) {
        channels.add(channel)
    }

    fun removeChannel(channel: String) {
        channels.remove(channel)
    }

    fun getChannels(): Set<String> {
        return channels
    }

    private suspend fun send(message: String) {
        writeChannel?.writeStringUtf8(message)
        writeChannel?.flush()
        logger.info("Sent: ${message.trimEnd()}")
    }

    private suspend fun receive(): String? {
        while (alive.get()) {
            try {
                val line = withTimeout(5000) {
                    readChannel?.readUTF8Line()
                }
                if (line != null) {
                    return line
                }
            }  catch (e: TimeoutCancellationException) {
                continue
            }
        }

        return null
    }

    suspend fun sendMessage(message: String) {
        try {
            outQueue.send(message)
        } catch (e: ClosedSendChannelException) {
            logger.info("sending ${message.trimEnd()}: Already closed: $e")
        }
    }

    private suspend fun receiveMessage(): Message? {
        val line = receive()
        return line?.let {
            logger.info("Received: $it")
            val message = Parser.parse(it)
            message
        }
    }

    fun close() {
        logger.info { "Closing client connection $nick!$user@$hostname"}
        outQueue.close()
        socket?.close()
        alive.set(false)
    }

    private suspend fun processOutQueue() {
        for (message in outQueue) {
            if (!alive.get()) {
                break
            }
            send(message)
        }
    }

    private suspend fun receiver() {
        while (alive.get()) {
            val message = receiveMessage()
            if (message == null) {
                logger.info { "Received null" }
                break
            }
            commandChannel.send(Command.Message(this, message))
        }
        throw IOException("EOF received")
    }

    private suspend fun healthcheckTicker() {
        while (alive.get()) {
            commandChannel.send(Command.Healthcheck(this))
            val deadline = System.currentTimeMillis() + 30000
            while (System.currentTimeMillis() < deadline && alive.get()) {
                delay(500)
            }
        }
    }

    suspend fun handle() {
        try {
            coroutineScope {
                launch { processOutQueue() }
                launch { healthcheckTicker() }
                launch { receiver() }
            }
        } catch (e: IOException) {
            logger.info { "IOException" }
        }

        alive.set(false)
        commandChannel.send(Command.Close(this))
        logger.info("Client disconnected $nick!$user@$hostname")
    }
}