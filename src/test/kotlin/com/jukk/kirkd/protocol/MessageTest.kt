package com.jukk.kirkd.protocol

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MessageTest : FunSpec({
    test("serializing PRIVMSG") {
        val message = Message.Privmsg("nick!user@host", "#channel", "hello yeah")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host PRIVMSG #channel :hello yeah\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing JOIN") {
        val message = Message.Join("nick!user@host", "#channel")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host JOIN #channel\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing PART") {
        val message = Message.Part("nick!user@host", "#channel", null)
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host PART #channel\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing PART with reason") {
        val message = Message.Part("nick!user@host", "#channel", "reason")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host PART #channel :reason\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing PING") {
        val message = Message.Ping("nick!user@host", "1234")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host PING 1234\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing PONG") {
        val message = Message.Pong("nick!user@host", "1234")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host PONG 1234\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing QUIT") {
        val message = Message.Quit("nick!user@host", "bye")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host QUIT :bye\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing NICK") {
        val message = Message.Nick("nick!user@host", "newnick")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host NICK newnick\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing TOPIC") {
        val message = Message.Topic("nick!user@host", "#channel", "new topic")
        val serialized = message.serialize()
        serialized shouldBe ":nick!user@host TOPIC #channel :new topic\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing TOPICREPLY") {
        val message = Message.TopicReply("server", "#channel", "tester", "new topic")
        val serialized = message.serialize()
        serialized shouldBe ":server 332 tester #channel :new topic\r\n"
        Parser.parse(serialized) shouldBe message
    }

    test("serializing USERS") {
        val message = Message.Users("server", "#channel", "tester", listOf("user1", "user2"))
        val serialized = message.serialize()
        serialized shouldBe ":server 353 tester @ #channel :user1 user2\r\n"
        Parser.parse(serialized) shouldBe message
    }
})
