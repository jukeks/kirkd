package com.jukk.kirkd.server

import com.jukk.kirkd.protocol.Message
import com.jukk.kirkd.protocol.Parser
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServerTest : FunSpec({
    test("handle client") {
        val port = 9009
        val server = Server("127.0.0.1", port)

        val serverJob = launch {server.start(this) }

        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect("127.0.0.1", port)

        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)

        val nick = Message.Nick("", "kirk")
        val user = Message.User("user", "*", "0", "kirk the kirk")
        val join = Message.Join("", "#test")

        sendChannel.writeStringUtf8(nick.serialize())
        sendChannel.writeStringUtf8(user.serialize())

        var welcomeFound = false
        for (i in 0..10) {
            val line = receiveChannel.readUTF8Line() ?: break
            val msg = Parser.parse(line)
            when (msg) {
                is Message.EndOfMotd -> {
                    msg.nick shouldBe "kirk"
                    welcomeFound = true
                    break
                }
            }
        }
        welcomeFound shouldBe true

        sendChannel.writeStringUtf8(join.serialize())

        var endOfNamesFound = true
        for (i in 0..10) {
            val line = receiveChannel.readUTF8Line() ?: break
            val msg = Parser.parse(line)
            when (msg) {
                is Message.EndOfNames -> {
                    msg.nick shouldBe "kirk"
                    endOfNamesFound = true
                    break
                }
            }
        }

        endOfNamesFound shouldBe true
        serverJob.cancel()
    }
})