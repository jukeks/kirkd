package com.jukk.kirkd.protocol

abstract class Message private constructor() {
    abstract fun fromAtoms(input: Atoms): Message
    abstract fun toAtoms(): Atoms
    protected fun formatNick(nick: String?): String {
        if (nick == null) {
            return "*"
        }
        return nick.ifEmpty { "*" }
    }

    class User(val user: String, val host: String, val servername: String, val realName: String) : Message() {
        override fun fromAtoms(input: Atoms): Message =
            User(input.prefix, input.params[0], input.params[1], input.params[2])

        override fun toAtoms(): Atoms = Atoms("", "USER", listOf(user, host, servername), realName)
    }

    class Privmsg(val prefix: String, val target: String, val content: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Privmsg(input.prefix, input.params[0], input.params[1])
        override fun toAtoms(): Atoms = Atoms(prefix, "PRIVMSG", listOf(target), content)
    }

    class Join(val prefix: String, val channel: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Join(input.prefix, input.params[0])
        override fun toAtoms(): Atoms = Atoms(prefix, "JOIN", listOf(channel))
    }

    class Part(val prefix: String, val channel: String, val message: String?) : Message() {
        override fun fromAtoms(input: Atoms): Message = Part(input.prefix, input.params[0], input.trailing)
        override fun toAtoms(): Atoms = Atoms(prefix, "PART", listOf(channel), message ?: "")
    }

    class Ping(val prefix: String, val id: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Ping(input.prefix, input.params[0])
        override fun toAtoms(): Atoms = Atoms(prefix, "PING", listOf(id))
    }

    class Pong(val prefix: String, val id: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Pong(input.prefix, input.params[0])
        override fun toAtoms(): Atoms = Atoms(prefix, "PONG", listOf(id))
    }

    class Quit(val prefix: String, val message: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Quit(input.prefix, input.trailing)
        override fun toAtoms(): Atoms = Atoms(prefix, "QUIT", emptyList(), message)
    }

    class Nick(val prefix: String, val nick: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Nick(input.prefix, input.params[0])
        override fun toAtoms(): Atoms = Atoms(prefix, "NICK", listOf(nick))
    }

    class Topic(val prefix: String, val channel: String, val topic: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Topic(input.prefix, input.params[0], input.params[1])
        override fun toAtoms(): Atoms = Atoms(prefix, "TOPIC", listOf(channel), topic)
    }

    class TopicReply(val prefix: String, val channel: String, val nick: String, val topic: String) : Message() {
        override fun fromAtoms(input: Atoms): Message =
            TopicReply(input.prefix, input.params[1], input.params[0], input.trailing)

        override fun toAtoms(): Atoms = Atoms(prefix, "332", listOf(nick, channel, ), topic)
    }

    class Users(val prefix: String, val channel: String, val nick: String, val users: List<String>) : Message() {
        override fun fromAtoms(input: Atoms): Message =
            Users(input.prefix, input.params[2], input.params[0], input.params[3].split(" "))

        override fun toAtoms(): Atoms = Atoms(prefix, "353", listOf(nick, "@", channel), users.joinToString(" "))
    }

    class EndOfUsers(val prefix: String, val channel: String, val nick: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = EndOfUsers(input.prefix, input.params[0], input.params[1])
        override fun toAtoms(): Atoms = Atoms(prefix, "366", listOf(channel, nick))
    }

    class EndOfMotd(val prefix: String, val nick: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = EndOfMotd(input.prefix, input.params[0])
        override fun toAtoms(): Atoms = Atoms(prefix, "376", listOf(nick))
    }

    class Unknown(val prefix: String, val command: String, val params: List<String>) : Message() {
        override fun fromAtoms(input: Atoms): Message = Unknown(input.prefix, input.command, input.params)
        override fun toAtoms(): Atoms = Atoms(prefix, command, params)
    }

    class Cap(val prefix: String, val subcommand: String, val params: List<String>) : Message() {
        override fun fromAtoms(input: Atoms): Message = Cap(input.prefix, input.params[0], input.params.drop(1))
        override fun toAtoms(): Atoms = Atoms(prefix, "CAP", listOf(subcommand) + params)
    }

    class Welcome(val prefix: String, val nick: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = Welcome(input.prefix, input.params[0])
        override fun toAtoms(): Atoms = Atoms(prefix, "001", listOf(nick))
    }

    class NickInUse(val prefix: String, val nick: String?, val newNick: String) : Message() {
        override fun fromAtoms(input: Atoms): Message = NickInUse(input.prefix, input.params[0], input.params[1])
        override fun toAtoms(): Atoms = Atoms(prefix, "433", listOf(formatNick(nick), newNick))
    }

    class RegisterFirst(val prefix: String, val nick: String?) : Message() {
        override fun fromAtoms(input: Atoms): Message = RegisterFirst(input.prefix, input.params[0])
        override fun toAtoms(): Atoms = Atoms(prefix, "451", listOf(formatNick(nick)))
    }
}
