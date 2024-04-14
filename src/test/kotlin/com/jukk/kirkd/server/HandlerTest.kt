package com.jukk.kirkd.server

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import com.jukk.kirkd.client.Client
import com.jukk.kirkd.protocol.Message
import io.kotest.matchers.collections.shouldContain
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

    fun newRegisteredClient(
        handler: Handler,
        state: State,
        nick: String,
        channels: List<String> = emptyList(),
        user: String = "user",
        realname: String = "realname",
        hostname: String = "hostname"
    ): Client {
        val client = newClient()
        val nickMsg = Message.Nick("", nick)
        handler.handle(Command.Message(client, nickMsg))
        val userMsg = Message.User(user, realname, "server", hostname)
        handler.handle(Command.Message(client, userMsg))
        client.isRegistered() shouldBe true
        client.hasAllInfo() shouldBe true

        for (channel in channels) {
            val join = Message.Join("", channel)
            handler.handle(Command.Message(client, join))
            state.getChannel(channel)!!.getClients() shouldContain client
        }

        return client
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

    test("user registration") {
        val client = newClient()
        val state = State()
        val handler = Handler("testserver", state)

        val nick = Message.Nick("", "tester1")
        handler.handle(Command.Message(client, nick))
        val user = Message.User("user", "host", "testserver", "realname")
        val output = handler.handle(Command.Message(client, user))

        client.getUser() shouldBe "user"
        client.getRealName() shouldBe "realname"
        client.getNick() shouldBe "tester1"

        client.isRegistered() shouldBe true
        client.hasAllInfo() shouldBe true

        output.size shouldBe 1
        output[0].clients.size shouldBe 1
        output[0].clients[0] shouldBe client
        val response1 = output[0].messages[0] as Message.Welcome
        response1.nick shouldBe "tester1"
        val response2 = output[0].messages.last() as Message.EndOfMotd
        response2.nick shouldBe "tester1"

        state.getClient("tester1") shouldBe client
    }

    test("registeredClient helper") {
        val state = State()
        newRegisteredClient(Handler("testserver", state), state, "tester1")
    }

    test("join") {
        val testChannel = "#test"
        val state = State()
        val handler = Handler("testserver", state)
        val client = newRegisteredClient(handler, state, "tester1")

        val join = Message.Join("", testChannel)
        val output = handler.handle(Command.Message(client, join))

        output.size shouldBe 2
        output[0].clients shouldContain client
        val joinAnnouncement = output[0].messages[0] as Message.Join
        joinAnnouncement.prefix shouldBe client.getFullmask()
        joinAnnouncement.channel shouldBe "#test"

        val otherClient = newRegisteredClient(handler, state,"tester2")
        val otherJoin = Message.Join("", testChannel)
        val otherOutput = handler.handle(Command.Message(otherClient, otherJoin))

        otherOutput.size shouldBe 2
        val otherJoinAnnouncement = otherOutput[0].messages[0] as Message.Join
        otherOutput[0].clients shouldContain otherClient
        otherOutput[0].clients shouldContain client

        otherJoinAnnouncement.prefix shouldBe otherClient.getFullmask()
        otherJoinAnnouncement.channel shouldBe "#test"

        val channel = state.getChannel(testChannel)!!
        channel.getClients().size shouldBe 2
        channel.getClients() shouldContain client
        channel.getClients() shouldContain otherClient

        val usersMessage = otherOutput[1].messages[0] as Message.Users
        usersMessage.prefix shouldBe "testserver"
        usersMessage.channel shouldBe testChannel
        usersMessage.users shouldContain client.getNick()
        usersMessage.users shouldContain otherClient.getNick()

        val endOfUsersMessage = otherOutput[1].messages[1] as Message.EndOfUsers
        endOfUsersMessage.prefix shouldBe "testserver"
        endOfUsersMessage.channel shouldBe testChannel
    }

    test("part") {
        val testChannel = "#test"
        val state = State()
        val handler = Handler("testserver", state)
        val client = newRegisteredClient(handler, state, "tester1")

        val join = Message.Join("", testChannel)
        handler.handle(Command.Message(client, join))
        state.getChannel(testChannel)!!.getClients().size shouldBe 1

        val part = Message.Part("", testChannel, null)
        val output = handler.handle(Command.Message(client, part))

        output.size shouldBe 1
        output[0].clients shouldContain client
        val partAnnouncement = output[0].messages[0] as Message.Part
        partAnnouncement.prefix shouldBe client.getFullmask()
        partAnnouncement.channel shouldBe "#test"

        val channel = state.getChannel(testChannel)
        channel shouldBe null
    }

    test("channel message with two clients") {
        val testChannel = "#test"
        val state = State()
        val handler = Handler("testserver", state)
        val client1 = newRegisteredClient(handler, state,"tester1", listOf(testChannel))
        val client2 = newRegisteredClient(handler, state, "tester2", listOf(testChannel))

        val message = Message.Privmsg("", testChannel, "hello")
        val output = handler.handle(Command.Message(client1, message))

        output.size shouldBe 1
        output[0].clients.size shouldBe 1
        output[0].clients shouldContain client2
        val privmsg = output[0].messages[0] as Message.Privmsg
        privmsg.prefix shouldBe client1.getFullmask()
        privmsg.target shouldBe testChannel
        privmsg.content shouldBe "hello"
    }

    test("access control") {
        val state = State()
        val handler = Handler("testserver", state)
        val client = newClient()

        val message = Message.Join("", "#test")
        val output = handler.handle(Command.Message(client, message))

        output.size shouldBe 1
        state.getChannel("#test") shouldBe null

        output[0].clients.size shouldBe 1
        output[0].clients shouldContain client
        output[0].messages[0] as Message.RegisterFirst
    }
})