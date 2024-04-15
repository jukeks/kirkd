package com.jukk.kirkd.protocol

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MessageTest : FunSpec({
    test("serializing PRIVMSG") {
        val serialized = Message.Privmsg("nick!user@host", "#channel", "hello yeah").serialize()
        serialized shouldBe ":nick!user@host PRIVMSG #channel :hello yeah\r\n"
    }

    test("serializing JOIN") {
        val serialized = Message.Join("nick!user@host", "#channel").serialize()
        serialized shouldBe ":nick!user@host JOIN #channel\r\n"
    }

    test("serializing PART") {
        val serializedWithReason = Message.Part("nick!user@host", "#channel", "reason").serialize()
        serializedWithReason shouldBe ":nick!user@host PART #channel :reason\r\n"

        val serialized = Message.Part("nick!user@host", "#channel", null).serialize()
        serialized shouldBe ":nick!user@host PART #channel\r\n"
    }

    test("serializing PING") {
        val serialized = Message.Ping("nick!user@host", "1234").serialize()
        serialized shouldBe ":nick!user@host PING 1234\r\n"
    }

    test("serializing PONG") {
        val serialized = Message.Pong("nick!user@host", "1234").serialize()
        serialized shouldBe ":nick!user@host PONG 1234\r\n"
    }

    test("serializing QUIT") {
        val serialized = Message.Quit("nick!user@host", "bye").serialize()
        serialized shouldBe ":nick!user@host QUIT :bye\r\n"
    }

    test("serializing NICK") {
        val serialized = Message.Nick("nick!user@host", "newnick").serialize()
        serialized shouldBe ":nick!user@host NICK newnick\r\n"
    }

    test("serializing TOPIC") {
        val serialized = Message.Topic("nick!user@host", "#channel", "new topic").serialize()
        serialized shouldBe ":nick!user@host TOPIC #channel :new topic\r\n"
    }

    test("serializing TOPICREPLY") {
        val serialized = Message.TopicReply("server", "#channel", "tester", "new topic").serialize()
        serialized shouldBe ":server 332 tester #channel :new topic\r\n"
    }

    test("serializing USERS") {
        val serialized =
            Message.Users("server", "#channel", "tester", listOf("user1", "user2")).serialize()
        serialized shouldBe ":server 353 tester @ #channel :user1 user2\r\n"
    }
})

