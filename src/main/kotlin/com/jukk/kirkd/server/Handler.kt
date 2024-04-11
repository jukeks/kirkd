package com.jukk.kirkd.server

import java.time.Instant
import com.jukk.kirkd.client.Client
import com.jukk.kirkd.protocol.Message
import com.jukk.kirkd.protocol.ServerMessage
import kotlinx.coroutines.channels.Channel
import com.jukk.kirkd.server.Channel as ServerChannel


class Handler(
    private val serverIdentity: String,
    private val state: State,
) {
    val commandChannel: Channel<Command> = Channel(Channel.UNLIMITED)

    suspend fun handle() {
        for (command in commandChannel) {
            when (command) {
                is Command.Message -> handleMessage(command.client, command.message)
                is Command.Close -> handleClose(command.client)
                is Command.Healthcheck -> handleHealthcheck(command.client)
            }
        }
    }

    suspend fun handleHealthcheck(client: Client) {
        val ts = Instant.now().toEpochMilli()
        val ping = Message.Ping(serverIdentity, ts.toString())
        val raw = ServerMessage.serialize(ping)
        client.sendMessage(raw)
    }

    suspend fun handleClose(client: Client) {
        state.removeClient(client)
        client.close()
    }

    suspend fun handleMessage(client: Client, message: Message) {
        when (message) {
            is Message.Nick -> handleNick(client, message)
            is Message.User -> handleUser(client, message)
            is Message.Join -> handleJoin(client, message)
            is Message.Part -> handlePart(client, message)
            is Message.Privmsg -> handlePrivmsg(client, message)
            is Message.Ping -> handlePing(client, message)
            is Message.Cap -> handleCap(client, message)
        }
    }

    suspend fun handleNick(client: Client, message: Message.Nick) {
        if (state.addNewNick(message.nick).getOrNull() == null) {
            val nickError = Message.NickInUse(serverIdentity, message.nick)
            val raw = ServerMessage.serialize(nickError)
            client.sendMessage(raw)
            return
        }

        client.setNick(message.nick)
    }

    suspend fun handleUser(client: Client, message: Message.User) {
        client.setUser(message.user)
        client.setRealName(message.realName)
        state.addClient(client)

        val welcome = Message.Welcome(serverIdentity, client.getNick())
        val endOfMotd = Message.EndOfMotd(serverIdentity, client.getNick())

        listOf(welcome, endOfMotd).map {
            val raw = ServerMessage.serialize(it)
            client.sendMessage(raw)
        }
    }

    suspend fun handleJoin(client: Client, message: Message.Join) {
        var channel = state.getChannel(message.channel)
        if (channel == null) {
            channel = ServerChannel(message.channel, mutableSetOf())
            state.addChannel(channel)
        }
        channel.addClient(client)

        val proxiedPart = Message.Join(client.getFullmask(), message.channel)
        val raw = ServerMessage.serialize(proxiedPart)
        for (userClient in channel.getClients()) {
            if (userClient == client) continue
            userClient.sendMessage(raw)
        }

        val usersMsg =
            Message.Users(serverIdentity, message.channel, client.getNick(), channel.getClients().map { it.getNick() })
        val usersRaw = ServerMessage.serialize(usersMsg)
        client.sendMessage(usersRaw)
        val endOfUsers = Message.EndOfUsers(serverIdentity, message.channel, client.getNick())
        val endOfUsersRaw = ServerMessage.serialize(endOfUsers)
        client.sendMessage(endOfUsersRaw)
    }

    suspend fun handlePart(client: Client, message: Message.Part) {
        val channel = state.getChannel(message.channel) ?: return
        channel.removeClient(client)

        if (channel.getClients().isEmpty()) {
            state.removeChannel(channel)
        }

        val proxiedPart = Message.Part(client.getFullmask(), message.channel)
        val raw = ServerMessage.serialize(proxiedPart)
        for (userClient in channel.getClients()) {
            userClient.sendMessage(raw)
        }
    }

    suspend fun handlePrivmsg(client: Client, message: Message.Privmsg) {
        val channel = state.getChannel(message.target)
        if (channel == null) {
            val targetClient = state.getClient(message.target)
            val privmsg = Message.Privmsg(client.getFullmask(), message.target, message.content)
            val raw = ServerMessage.serialize(privmsg)
            targetClient?.sendMessage(raw)
        } else {
            val privmsg = Message.Privmsg(client.getFullmask(), message.target, message.content)
            val raw = ServerMessage.serialize(privmsg)
            for (userClient in channel.getClients()) {
                if (userClient == client) continue
                userClient.sendMessage(raw)
            }
        }
    }

    suspend fun handlePing(client: Client, message: Message.Ping) {
        val pong = Message.Pong(serverIdentity, message.id)
        val raw = ServerMessage.serialize(pong)
        client.sendMessage(raw)
    }

    suspend fun handleCap(client: Client, message: Message.Cap) {
        if (message.subcommand == "LS") {
            val cap = Message.Cap(serverIdentity, "LS", emptyList())
            val raw = ServerMessage.serialize(cap)
            client.sendMessage(raw)
        } else if (message.subcommand == "REQ") {
            val nak = Message.Cap(serverIdentity, "NAK", emptyList())
            val raw = ServerMessage.serialize(nak)
            client.sendMessage(raw)
        }
    }
}