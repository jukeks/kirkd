package com.jukk.kirk.server

import com.jukk.kirk.protocol.Message
import com.jukk.kirk.client.Client

data class Command(val client: Client, val message: Message)