package com.jukk.kirkd.protocol

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ParserTest : FunSpec({
    test("parsing NICK") {
        val msg = "NICK test"
        val parsed = Parser.parse(msg) as Message.Nick
        parsed.nick shouldBe "test"
        parsed.prefix shouldBe ""
    }

    test("parsing USER") {
        val msg = "USER username 0 * :real name"
        val parsed = Parser.parse(msg) as Message.User
        parsed.user shouldBe "username"
        parsed.host shouldBe "0"
        parsed.realName shouldBe "real name"
    }

    test("parsing PRIVMSG") {
        val msg = "PRIVMSG #channel :just a few words"
        val parsed = Parser.parse(msg) as Message.Privmsg
        parsed.target shouldBe "#channel"
        parsed.content shouldBe "just a few words"
    }

    test("parsing JOIN") {
        val msg = "JOIN #channel"
        val parsed = Parser.parse(msg) as Message.Join
        parsed.channel shouldBe "#channel"
    }

    test("parsing PART") {
        val msg = "PART #channel"
        val parsed = Parser.parse(msg) as Message.Part
        parsed.channel shouldBe "#channel"
    }

    test("parsing PING") {
        val msg = "PING 123"
        val parsed = Parser.parse(msg) as Message.Ping
        parsed.id shouldBe "123"
    }

    test("parsing PONG") {
        val msg = "PONG 123"
        val parsed = Parser.parse(msg) as Message.Pong
        parsed.id shouldBe "123"
    }

    test("parsing QUIT") {
        val msg = "QUIT :bye"
        val parsed = Parser.parse(msg) as Message.Quit
        parsed.message shouldBe "bye"
    }

    test("parsing TOPIC") {
        val msg = "TOPIC #channel :new topic"
        val parsed = Parser.parse(msg) as Message.Topic
        parsed.channel shouldBe "#channel"
        parsed.topic shouldBe "new topic"
    }

    test("parsing CAP") {
        val msg = "CAP LS :multi-prefix"
        val parsed = Parser.parse(msg) as Message.Cap
        parsed.subcommand shouldBe "LS"
        parsed.params shouldBe listOf("multi-prefix")
    }
})