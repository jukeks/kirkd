package com.jukk.kirkd.server

import com.jukk.kirkd.client.Client

open class Command private constructor() {
    class Message(val client: Client, val message: com.jukk.kirkd.protocol.Message) : Command()
    class Close(val client: Client) : Command()
    class Healthcheck(val client: Client) : Command()
}

data class CommandOutput(
    val clients: List<Client>, val messages: List<com.jukk.kirkd.protocol.Message>
) {
    constructor(client: Client, message: com.jukk.kirkd.protocol.Message) : this(listOf(client), listOf(message))
    constructor(clients: List<Client>, message: com.jukk.kirkd.protocol.Message) : this(clients, listOf(message))
    constructor(client: Client, messages: List<com.jukk.kirkd.protocol.Message>) : this(listOf(client), messages)
}