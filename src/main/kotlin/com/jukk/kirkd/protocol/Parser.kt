package com.jukk.kirkd.protocol

data class Atoms(
    val prefix: String,
    val command: String,
    val params: List<String>
) {
    override fun toString(): String {
        if (prefix.isNotEmpty()) {
            return "Atoms(prefix='$prefix', command='$command', params=$params)"
        }
        return "Atoms(command='$command', params=$params)"
    }
}

open class Message private constructor() {
    class User(val user: String, val host: String, val realName: String) : Message()
    class Privmsg(val prefix: String, val target: String, val content: String) : Message()
    class Join(val prefix: String, val channel: String) : Message()
    class Part(val prefix: String, val channel: String) : Message()
    class Ping(val prefix: String, val id: String) : Message()
    class Pong(val prefix: String, val id: String) : Message()
    class Quit(val prefix: String, val message: String) : Message()
    class Nick(val prefix: String, val nick: String) : Message()
    class Topic(val prefix: String, val channel: String, val topic: String) : Message()
    class TopicReply(val prefix: String, val channel: String, val topic: String) : Message()
    class Users(val prefix: String, val channel: String, val nick: String, val users: List<String>) : Message()
    class EndOfUsers(val prefix: String, val channel: String, val nick: String) : Message()
    class EndOfMotd(val prefix: String, val nick: String) : Message()
    class Unknown(val prefix: String, val command: String, val params: List<String>) : Message()
    class Cap(val prefix: String, val subcommand: String, val params: List<String>) : Message()
    class Welcome(val prefix: String, val nick: String) : Message()
}

object ClientParser {
    fun strippingPartition(input: String): Pair<String, String> {
        val components = input.split(" ", limit = 2)
        return Pair(components[0], components.getOrNull(1)?.trimStart() ?: "")
    }

    fun parseParams(paramsRaw: String): List<String> {
        val params = mutableListOf<String>()
        var rest = paramsRaw
        while (rest.isNotEmpty()) {
            if (rest.startsWith(":")) {
                params.add(rest.substring(1))
                break
            }

            val (param, newRest) = strippingPartition(rest)
            params.add(param)
            rest = newRest
        }

        return params
    }

    fun atomsFromString(input: String): Atoms {
        if (input.startsWith(":")) {
            val (prefix, rest) = strippingPartition(input)
            val (command, params) = strippingPartition(rest)
            return Atoms(prefix, command, parseParams(params))
        }

        val prefix = ""
        val (command, params) = strippingPartition(input)
        return Atoms(prefix, command, parseParams(params))
    }

    fun parse(input: String): Message {
        val atoms = atomsFromString(input)
        return when (atoms.command) {
            "USER" -> {
                val (nick, user, host) = atoms.params
                Message.User(nick, user, host)
            }

            "PRIVMSG" -> Message.Privmsg(atoms.prefix, atoms.params[0], atoms.params[1])
            "JOIN" -> Message.Join(atoms.prefix, atoms.params[0])
            "PART" -> Message.Part(atoms.prefix, atoms.params[0])
            "PING" -> Message.Ping(atoms.prefix, atoms.params[0])
            "PONG" -> Message.Pong(atoms.prefix, atoms.params[0])
            "QUIT" -> Message.Quit(atoms.prefix, atoms.params[0])
            "NICK" -> Message.Nick(atoms.prefix, atoms.params[0])
            "TOPIC" -> Message.Topic(atoms.prefix, atoms.params[0], atoms.params[1])
            "CAP" -> Message.Cap(atoms.prefix, atoms.params[0], atoms.params.drop(1))
            else -> Message.Unknown(atoms.prefix, atoms.command, atoms.params)
        }
    }
}