package com.jukk.kirkd.server

import com.jukk.kirkd.client.Client

open class Command private constructor() {
    class Message(val client: Client, val message: com.jukk.kirkd.protocol.Message) : Command()
    class Close(val client: Client) : Command()
    class Healthcheck(val client: Client) : Command()
}
