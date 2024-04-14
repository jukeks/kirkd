package com.jukk.kirkd.protocol

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AtomsTest : FunSpec({
    test("serialize") {
        val atoms = Atoms("prefix", "command", listOf("param1", "param2"), "param3")
        atoms.serialize() shouldBe ":prefix command param1 param2 :param3\r\n"
    }

    test("serialize without prefix") {
        val atoms = Atoms("", "command", listOf("param1", "param2"), "param3")
        atoms.serialize() shouldBe "command param1 param2 :param3\r\n"
    }

    test("serialize without params") {
        val atoms = Atoms("prefix", "command", emptyList())
        atoms.serialize() shouldBe ":prefix command\r\n"
    }

    test("serialize with trailing") {
        val atoms = Atoms("prefix", "command", trailing = "param1 param2 param3")
        atoms.serialize() shouldBe ":prefix command :param1 param2 param3\r\n"
    }
})