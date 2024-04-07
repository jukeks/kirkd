package com.jukk.kirk.server

import com.jukk.kirk.client.Client
import com.jukk.kirk.protocol.Message
import com.jukk.kirk.protocol.ServerMessage
import kotlinx.coroutines.channels.Channel
import com.jukk.kirk.server.Channel as ServerChannel


class Handler(
    val serverIdentity: String,
    val state: State,
) {
    val commandChannel: Channel<Command> = Channel(Channel.UNLIMITED)

    suspend fun handle() {
        for (command in commandChannel) {
            val message = command.message
            val client = command.client
            when (message) {
                is Message.Nick -> handleNick(client, message)
                is Message.User -> handleUser(client, message)
                is Message.Join -> handleJoin(client, message)
                is Message.Part -> handlePart(client, message)
                is Message.Privmsg -> handlePrivmsg(client, message)
            }
        }
    }

    suspend fun handleNick(client: Client, message: Message.Nick) {
        client.setNick(message.nick) // TODO check if nick is already in use
    }

    suspend fun handleUser(client: Client, message: Message.User) {
        client.setUser(message.user)
        client.setRealName(message.realName)
        state.addClient(client)

        val endOfMotd = Message.EndOfMotd(serverIdentity, client.getNick())
        val raw = ServerMessage.serialize(endOfMotd)
        client.sendMessage(raw)
    }

    suspend fun handleJoin(client: Client, message: Message.Join) {
        var channel = state.getChannel(message.channel)
        if (channel == null) {
            channel = ServerChannel(message.channel, mutableListOf(client))
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
            targetClient?.sendMessage(message.content)
        } else {
            for (userClient in channel.getClients()) {
                userClient.sendMessage(message.content)
            }
        }
    }
}