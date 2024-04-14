package com.jukk.kirkd.protocol

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ServerMessageTest : FunSpec({
    test("serializing PRIVMSG") {
        val serialized = ServerMessage.serialize(Message.Privmsg("nick!user@host", "#channel", "hello yeah"))
        serialized shouldBe ":nick!user@host PRIVMSG #channel :hello yeah\r\n"
    }

    test("serializing JOIN") {
        val serialized = ServerMessage.serialize(Message.Join("nick!user@host", "#channel"))
        serialized shouldBe ":nick!user@host JOIN #channel\r\n"
    }

    test("serializing PART") {
        val serializedWithReason = ServerMessage.serialize(Message.Part("nick!user@host", "#channel", "reason"))
        serializedWithReason shouldBe ":nick!user@host PART #channel :reason\r\n"

        val serialized = ServerMessage.serialize(Message.Part("nick!user@host", "#channel", null))
        serialized shouldBe ":nick!user@host PART #channel\r\n"
    }

    test("serializing PING") {
        val serialized = ServerMessage.serialize(Message.Ping("nick!user@host", "1234"))
        serialized shouldBe ":nick!user@host PING 1234\r\n"
    }

    test("serializing PONG") {
        val serialized = ServerMessage.serialize(Message.Pong("nick!user@host", "1234"))
        serialized shouldBe ":nick!user@host PONG 1234\r\n"
    }

    test("serializing QUIT") {
        val serialized = ServerMessage.serialize(Message.Quit("nick!user@host", "bye"))
        serialized shouldBe ":nick!user@host QUIT :bye\r\n"
    }

    test("serializing NICK") {
        val serialized = ServerMessage.serialize(Message.Nick("nick!user@host", "newnick"))
        serialized shouldBe ":nick!user@host NICK newnick\r\n"
    }

    test("serializing TOPIC") {
        val serialized = ServerMessage.serialize(Message.Topic("nick!user@host", "#channel", "new topic"))
        serialized shouldBe ":nick!user@host TOPIC #channel :new topic\r\n"
    }

    test("serializing TOPICREPLY") {
        val serialized = ServerMessage.serialize(Message.TopicReply("server", "#channel", "tester", "new topic"))
        serialized shouldBe ":server 332 tester #channel :new topic\r\n"
    }

    test("serializing USERS") {
        val serialized =
            ServerMessage.serialize(Message.Users("server", "#channel", "tester", listOf("user1", "user2")))
        serialized shouldBe ":server 353 tester @ #channel :user1 user2\r\n"
    }
})

