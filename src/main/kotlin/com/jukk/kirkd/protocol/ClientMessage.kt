package com.jukk.kirkd.protocol

object ClientMessage {
    fun parse(input: String): Message {
        val atoms = Parser.atomsFromString(input)
        return when (atoms.command) {
            "USER" -> {
                val (nick, user, host, realname) = atoms.params
                Message.User(nick, user, host, realname)
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

