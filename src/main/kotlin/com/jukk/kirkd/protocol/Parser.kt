package com.jukk.kirkd.protocol

object Parser {
    private fun strippingPartition(input: String): Pair<String, String> {
        val components = input.split(" ", limit = 2)
        return Pair(components[0], components.getOrNull(1)?.trimStart() ?: "")
    }

    private fun parseParams(paramsRaw: String): List<String> {
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
            return Atoms(prefix.drop(1), command, parseParams(params))
        }

        val prefix = ""
        val (command, params) = strippingPartition(input)
        return Atoms(prefix, command, parseParams(params))
    }

    fun parse(input: String): Message {
        val atoms = atomsFromString(input.trimEnd())
        return when (atoms.command) {
            "USER" -> Message.User.fromAtoms(atoms)
            "PRIVMSG" -> Message.Privmsg.fromAtoms(atoms)
            "JOIN" -> Message.Join.fromAtoms(atoms)
            "PART" -> Message.Part.fromAtoms(atoms)
            "PING" -> Message.Ping.fromAtoms(atoms)
            "PONG" -> Message.Pong.fromAtoms(atoms)
            "QUIT" -> Message.Quit.fromAtoms(atoms)
            "NICK" -> Message.Nick.fromAtoms(atoms)
            "TOPIC" -> Message.Topic.fromAtoms(atoms)
            "CAP" -> Message.Cap.fromAtoms(atoms)
            "332" -> Message.TopicReply.fromAtoms(atoms)
            "353" -> Message.Names.fromAtoms(atoms)
            "366" -> Message.EndOfNames.fromAtoms(atoms)
            else -> Message.Unknown(atoms.prefix, atoms.command, atoms.params)
        }
    }
}