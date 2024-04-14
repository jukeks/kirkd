package com.jukk.kirkd.protocol

object ServerMessage {
    private fun formatNick(nick: String?): String {
        if (nick == null) {
            return "*"
        }
        return nick.ifEmpty { "*" }
    }

    fun serialize(message: Message): String {
        val atoms = when (message) {
            is Message.Privmsg -> Atoms(message.prefix, "PRIVMSG", listOf(message.target), message.content)
            is Message.Join -> Atoms(message.prefix, "JOIN", listOf(message.channel))
            is Message.Part -> Atoms(
                message.prefix, "PART", listOf(message.channel), message.message ?: ""
            )

            is Message.Ping -> Atoms(message.prefix, "PING", listOf(message.id))
            is Message.Pong -> Atoms(message.prefix, "PONG", listOf(message.id))
            is Message.Quit -> Atoms(message.prefix, "QUIT", trailing = message.message)
            is Message.Nick -> Atoms(message.prefix, "NICK", listOf(message.nick))
            is Message.Topic -> Atoms(message.prefix, "TOPIC", listOf(message.channel), message.topic)
            is Message.TopicReply -> Atoms(
                message.prefix, "332", listOf(message.nick, message.channel), message.topic
            )

            is Message.Users -> Atoms(
                message.prefix,
                "353",
                listOf(message.nick, "@", message.channel),
                message.users.joinToString(" "))

            is Message.EndOfUsers -> Atoms(
                message.prefix,
                "366",
                listOf(message.nick, message.channel), "End of /NAMES list."
            )

            is Message.EndOfMotd -> Atoms(
                message.prefix, "376", listOf(message.nick, "End of /MOTD command.")
            )

            is Message.Cap -> Atoms(
                message.prefix, "CAP", listOf("*", message.subcommand) + message.params
            )

            is Message.Welcome -> Atoms(
                message.prefix,
                "001",
                listOf(message.nick), "Welcome to the Internet Relay Network ${message.nick}"
            )

            is Message.NickInUse -> {
                Atoms(
                    message.prefix,
                    "433",
                    listOf(formatNick(message.nick), message.newNick), "Nickname already in use."
                )
            }

            is Message.RegisterFirst -> {
                Atoms(message.prefix, "451", listOf(formatNick(message.nick)), "Register first.")
            }

            else -> throw IllegalArgumentException("Unknown message type")
        }

        return atoms.serialize()
    }
}