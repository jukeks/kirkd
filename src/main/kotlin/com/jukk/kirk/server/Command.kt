package com.jukk.kirk.server

import com.jukk.kirk.protocol.Message
import com.jukk.kirk.client.Client

open class Command private constructor() {
    class Message(val client: Client, val message: com.jukk.kirk.protocol.Message) : Command()
    class Close(val client: Client) : Command()
    class Healthcheck(val client: Client) : Command()
}
