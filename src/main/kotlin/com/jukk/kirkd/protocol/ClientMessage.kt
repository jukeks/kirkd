package com.jukk.kirkd.protocol

object ClientMessage {
    fun parse(input: String): Message {
        val atoms = Parser.atomsFromString(input)
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
            else -> Message.Unknown(atoms.prefix, atoms.command, atoms.params)
        }
    }
}

