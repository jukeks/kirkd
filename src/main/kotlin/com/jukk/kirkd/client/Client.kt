package com.jukk.kirkd.client

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import com.jukk.kirkd.protocol.Message
import com.jukk.kirkd.protocol.Parser
import com.jukk.kirkd.server.Command
import io.ktor.util.network.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import mu.KotlinLogging
import java.net.SocketException


private val logger = KotlinLogging.logger {}

class Client(
    private val socket: Socket?,
    private val commandChannel: Channel<Command>,
    outBufferLen: Int = 400
) {
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
        return readChannel?.readUTF8Line()
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
        outQueue.close()
        socket?.close()
    }

    private suspend fun processOutQueue() {
        for (message in outQueue) {
            send(message)
        }
    }

    suspend fun healthcheckTicker() {
        while (true) {
            commandChannel.send(Command.Healthcheck(this))
            kotlinx.coroutines.delay(30000)
        }
    }

    suspend fun handle(scope: CoroutineScope) {
        val writerJob = scope.launch {
            processOutQueue()
        }

        val healthcheckJob = scope.launch { healthcheckTicker() }

        try {
            while (true) {
                val message = receiveMessage() ?: break
                commandChannel.send(Command.Message(this, message))
            }
        } catch (e: SocketException) {
            logger.info("Client error $nick!$user@$hostname: ${e.message}")
        }

        writerJob.cancel()
        healthcheckJob.cancel()
        commandChannel.send(Command.Close(this))

        logger.info("Client disconnected $nick!$user@$hostname")
    }
}