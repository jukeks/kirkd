package com.jukk.kirkd.server

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import com.jukk.kirkd.client.Client
import com.jukk.kirkd.protocol.Message
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import kotlinx.coroutines.channels.Channel
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class HandlerTest : FunSpec({
    fun newClient(): Client {
        return Client(null, Channel(Channel.UNLIMITED))
    }

    test("handle cap") {
        val client = newClient()
        val state = State()
        val handler = Handler("test", state)
        val capLs = Message.Cap("", "LS", emptyList())
        val output = handler.handle(Command.Message(client, capLs))

        output[0].clients[0] shouldBe client
        val message = output[0].messages[0] as Message.Cap
        message.prefix shouldBe "test"
        message.subcommand shouldBe "LS"
        message.params shouldBe emptyList()
    }

    test("handle cap req") {
        val client = newClient()
        val state = State()
        val handler = Handler("test", state)
        val capReq = Message.Cap("", "REQ", listOf("cap1", "cap2"))
        val output = handler.handle(Command.Message(client, capReq))

        output.size shouldBe 1
        output[0].clients.size shouldBe 1
        output[0].messages.size shouldBe 1
        output[0].clients[0] shouldBe client

        val message = output[0].messages[0] as Message.Cap
        message.prefix shouldBe "test"
        message.subcommand shouldBe "NAK"
        message.params shouldBe listOf()
    }

    test("nick") {
        val client = newClient()
        val state = State()
        state.addClient(client)
        val handler = Handler("test", state)

        val nick = Message.Nick("", "newnick")
        handler.handle(Command.Message(client, nick))

        client.getNick() shouldBe "newnick"
    }

    test("nick in use") {
        val client1 = newClient()
        val client2 = newClient()
        val state = State()
        state.addClient(client1)
        state.addClient(client2)
        val handler = Handler("test", state)

        val nick = Message.Nick("", "tester1")
        handler.handle(Command.Message(client1, nick))

        val nick2 = Message.Nick("", "tester1")
        val output = handler.handle(Command.Message(client2, nick2))

        client1.getNick() shouldBe "tester1"
        client2.getNick() shouldBe ""

        output.size shouldBe 1
        output[0].clients.size shouldBe 1
        output[0].messages.size shouldBe 1
        output[0].clients[0] shouldBe client2
        val response = output[0].messages[0] as Message.NickInUse
        response.nick shouldBe ""
        response.newNick shouldBe "tester1"
    }
})