package com.jukk.kirk.client

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import com.jukk.kirk.protocol.ClientParser
import com.jukk.kirk.protocol.Message
import com.jukk.kirk.server.Command
import kotlinx.coroutines.channels.Channel

class Client(
    private val socket: Socket,
    private val commandChannel: Channel<Command>,
) {
    private val readChannel: ByteReadChannel = socket.openReadChannel()
    private val writeChannel: ByteWriteChannel = socket.openWriteChannel()

    private val outQueue = Channel<String>(400)

    private var nick: String = ""
    private var user: String = ""
    private var realName: String = ""

    fun getFullmask(): String {
        return "$nick!$user@$realName"
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

    private suspend fun send(message: String) {
        writeChannel.writeStringUtf8(message)
        writeChannel.flush()
    }

    private suspend fun receive(): String? {
        return readChannel.readUTF8Line()
    }

    suspend fun sendMessage(message: String) {
        send(message)
    }

    suspend fun receiveMessage(): Message? {
        val line = receive()
        return line?.let {
            println("Received: $it")
            val message = ClientParser.parse(it)
            message
        }
    }

    suspend fun close() {
        socket.awaitClosed()
    }

    suspend fun handle() {
        while (true) {
            val message = receiveMessage() ?: break
            commandChannel.send(Command(this, message))
        }

        close()
    }
}