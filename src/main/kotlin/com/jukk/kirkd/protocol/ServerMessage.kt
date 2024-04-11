package com.jukk.kirkd.protocol

object ServerMessage {
    fun serialize(message: Message): String {
        val line = when (message) {
            is Message.Privmsg -> ":${message.prefix} PRIVMSG ${message.target} :${message.content}"
            is Message.Join -> ":${message.prefix} JOIN ${message.channel}"
            is Message.Part -> ":${message.prefix} PART ${message.channel}"
            is Message.Ping -> ":${message.prefix} PING ${message.prefix} :${message.id}"
            is Message.Pong -> ":${message.prefix} PONG ${message.prefix} :${message.id}"
            is Message.Quit -> ":${message.prefix} QUIT :${message.message}"
            is Message.Nick -> ":${message.prefix} NICK ${message.nick}"
            is Message.Topic -> ":${message.prefix} TOPIC ${message.channel} :${message.topic}"
            is Message.TopicReply -> ":${message.prefix} 332 ${message.channel} :${message.topic}"
            is Message.Users -> ":${message.prefix} 353 ${message.nick} @ ${message.channel} :${
                message.users.joinToString(
                    " "
                )
            }"

            is Message.EndOfUsers -> ":${message.prefix} 366 ${message.nick} @ ${message.channel} :End of /NAMES list"
            is Message.EndOfMotd -> ":${message.prefix} 376 ${message.nick} :End of /MOTD command"
            is Message.Cap ->
                ":${message.prefix} CAP * ${message.subcommand} :${message.params.joinToString(" ")}"
            is Message.Welcome ->
                ":${message.prefix} 001 ${message.nick} :Welcome to the Internet Relay Network ${message.nick}"
            is Message.NickInUse ->
                ":${message.prefix} 433 ${message.nick} :Nickname already in use"

            else -> throw IllegalArgumentException("Unknown message type")
        }

        return "$line\r\n"
    }
}