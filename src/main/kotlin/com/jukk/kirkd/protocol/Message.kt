package com.jukk.kirkd.protocol

abstract class Message private constructor() {
    companion object {
        fun fromAtoms(input: Atoms): Message = throw NotImplementedError()
    }

    abstract fun toAtoms(): Atoms
    fun serialize(): String = toAtoms().serialize()

    protected fun formatNick(nick: String?): String {
        if (nick == null) {
            return "*"
        }
        return nick.ifEmpty { "*" }
    }

    data class User(val user: String, val host: String, val servername: String, val realName: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message =
                User(input.params[0], input.params[1], input.params[2], input.params[3])
        }

        override fun toAtoms(): Atoms = Atoms("", "USER", listOf(user, host, servername), realName)
    }

    data class Privmsg(val prefix: String, val target: String, val content: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Privmsg(input.prefix, input.params[0], input.params[1])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "PRIVMSG", listOf(target), content)
    }

    data class Join(val prefix: String, val channel: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Join(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "JOIN", listOf(channel))
    }

    data class Part(val prefix: String, val channel: String, val message: String?) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Part(input.prefix, input.params[0], input.params.getOrNull(1))
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "PART", listOf(channel), message)
    }

    data class Ping(val prefix: String, val id: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Ping(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "PING", listOf(id))
    }

    data class Pong(val prefix: String, val id: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Pong(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "PONG", listOf(id))
    }

    data class Quit(val prefix: String, val message: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Quit(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "QUIT", emptyList(), message)
    }

    data class Nick(val prefix: String, val nick: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Nick(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "NICK", listOf(nick))
    }

    data class Topic(val prefix: String, val channel: String, val topic: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Topic(input.prefix, input.params[0], input.params[1])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "TOPIC", listOf(channel), topic)
    }

    data class TopicReply(val prefix: String, val channel: String, val nick: String, val topic: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message =
                TopicReply(input.prefix, input.params[1], input.params[0], input.params[2])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "332", listOf(nick, channel), topic)
    }

    data class Users(val prefix: String, val channel: String, val nick: String, val users: List<String>) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message =
                Users(input.prefix, input.params[2], input.params[0], input.params[3].split(" "))
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "353", listOf(nick, "@", channel), users.joinToString(" "))
    }

    data class EndOfUsers(val prefix: String, val channel: String, val nick: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = EndOfUsers(input.prefix, input.params[1], input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "366", listOf(nick,  channel), "End of /NAMES list.")
    }

    data class EndOfMotd(val prefix: String, val nick: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = EndOfMotd(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "376", listOf(nick))
    }

    data class Cap(val prefix: String, val nick: String?, val subcommand: String, val params: List<String>) :
        Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Cap(input.prefix, null, input.params[0], input.params.getOrNull(1)?.split(" ") ?: emptyList())
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "CAP", listOf(nick ?: "*", subcommand), params.joinToString(" "))
    }

    data class Welcome(val prefix: String, val nick: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Welcome(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "001", listOf(nick))
    }

    data class NickInUse(val prefix: String, val nick: String?, val newNick: String) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = NickInUse(input.prefix, input.params[0], input.params[1])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "433", listOf(formatNick(nick), newNick))
    }

    data class RegisterFirst(val prefix: String, val nick: String?) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = RegisterFirst(input.prefix, input.params[0])
        }

        override fun toAtoms(): Atoms = Atoms(prefix, "451", listOf(formatNick(nick)))
    }

    data class Unknown(val prefix: String, val command: String, val params: List<String>) : Message() {
        companion object {
            fun fromAtoms(input: Atoms): Message = Unknown(input.prefix, input.command, input.params)
        }

        override fun toAtoms(): Atoms = Atoms(prefix, command, params)
    }
}
