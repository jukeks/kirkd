package com.jukk.kirkd.protocol

object ServerMessage {
    private fun formatPrefix(prefix: String): String = if (prefix == "") "" else ":$prefix "

    fun serialize(message: Message): String {
        val line = when (message) {
            is Message.Privmsg -> "${formatPrefix(message.prefix)}PRIVMSG ${message.target} :${message.content}"
            is Message.Join -> "${formatPrefix(message.prefix)}JOIN ${message.channel}"
            is Message.Part -> {
                val content = if (message.message != null) " :${message.message}" else ""
                "${formatPrefix(message.prefix)}PART ${message.channel}$content"
            }

            is Message.Ping -> "${formatPrefix(message.prefix)}PING :${message.id}"
            is Message.Pong -> "${formatPrefix(message.prefix)}PONG :${message.id}"
            is Message.Quit -> "${formatPrefix(message.prefix)}QUIT :${message.message}"
            is Message.Nick -> "${formatPrefix(message.prefix)}NICK ${message.nick}"
            is Message.Topic -> "${formatPrefix(message.prefix)}TOPIC ${message.channel} :${message.topic}"
            is Message.TopicReply ->
                "${formatPrefix(message.prefix)}332 ${message.nick} ${message.channel} :${message.topic}"
            is Message.Users -> "${formatPrefix(message.prefix)}353 ${message.nick} @ ${message.channel} :${
                message.users.joinToString(
                    " "
                )
            }"

            is Message.EndOfUsers ->
                "${formatPrefix(message.prefix)}366 ${message.nick} ${message.channel} :End of /NAMES list"
            is Message.EndOfMotd -> "${formatPrefix(message.prefix)}376 ${message.nick} :End of /MOTD command"
            is Message.Cap ->
                "${formatPrefix(message.prefix)}CAP * ${message.subcommand} :${message.params.joinToString(" ")}"

            is Message.Welcome ->
                "${formatPrefix(message.prefix)}001 ${message.nick} :Welcome to the Internet Relay Network ${message.nick}"

            is Message.NickInUse -> {
                val nick = if (message.nick == "") "*" else message.nick
                "${formatPrefix(message.prefix)}433 ${nick} ${message.newNick} :Nickname already in use"
            }

            is Message.RegisterFirst -> {
                val nick = if (message.nick == "") "*" else message.nick
                "${formatPrefix(message.prefix)}451 $nick :Register first"
            }

            else -> throw IllegalArgumentException("Unknown message type")
        }

        return "$line\r\n"
    }
}