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

    suspend fun handlerLoop() {
        for (command in commandChannel) {
            val results = handle(command)
            for (result in results) {
                for (message in result.messages) {
                    val serialized = ServerMessage.serialize(message)
                    for (client in result.clients) {
                        client.sendMessage(serialized)
                    }
                }
            }
        }
    }

    fun handle(command: Command): List<CommandOutput> {
        return when (command) {
            is Command.Message -> handleMessage(command.client, command.message)
            is Command.Close -> handleClose(command.client)
            is Command.Healthcheck -> handleHealthcheck(command.client)
            else -> emptyList()
        }
    }

    fun handleHealthcheck(client: Client): List<CommandOutput> {
        val ts = Instant.now().toEpochMilli()
        return listOf(CommandOutput(client, Message.Ping(serverIdentity, ts.toString())))
    }

    fun handleClose(client: Client): List<CommandOutput> {
        state.removeClient(client)
        client.close()
        return emptyList() // TODO: send quit to all channels
    }

    fun handleMessage(client: Client, message: Message): List<CommandOutput> {
        return when (message) {
            is Message.Nick -> handleNick(client, message)
            is Message.User -> handleUser(client, message)
            is Message.Join -> handleJoin(client, message)
            is Message.Part -> handlePart(client, message)
            is Message.Privmsg -> handlePrivmsg(client, message)
            is Message.Ping -> handlePing(client, message)
            is Message.Cap -> handleCap(client, message)
            else -> emptyList()
        }
    }

    fun handleNick(client: Client, message: Message.Nick): List<CommandOutput> {
        if (state.addNewNick(message.nick).getOrNull() == null) {
            return listOf(
                CommandOutput(
                    listOf(client),
                    Message.NickInUse(serverIdentity, client.getNick(), message.nick)
                )
            )
        }

        client.setNick(message.nick)
        if (!client.isRegistered() && client.hasAllInfo()) {
            return handleRegistrationComplete(client)
        }

        // TODO: send NICK to all channels
        return emptyList()
    }

    private fun handleRegistrationComplete(client: Client): List<CommandOutput> {
        state.addClient(client)
        client.setRegistered()
        val welcome = Message.Welcome(serverIdentity, client.getNick())
        val endOfMotd = Message.EndOfMotd(serverIdentity, client.getNick())

        return listOf(CommandOutput(client, listOf(welcome, endOfMotd)))
    }

    fun handleUser(client: Client, message: Message.User): List<CommandOutput> {
        client.setUser(message.user)
        client.setRealName(message.realName)

        if (client.isRegistered() || !client.hasAllInfo()) {
            return emptyList()
        }

        return handleRegistrationComplete(client)
    }

    fun handleJoin(client: Client, message: Message.Join): List<CommandOutput> {
        var channel = state.getChannel(message.channel)
        if (channel == null) {
            channel = ServerChannel(message.channel, mutableSetOf())
            state.addChannel(channel)
        }
        channel.addClient(client)

        val proxiedJoin = Message.Join(client.getFullmask(), message.channel)
        val channelClients = channel.getClients().toList()

        val usersMsg =
            Message.Users(serverIdentity, message.channel, client.getNick(), channel.getClients().map { it.getNick() })
        val endOfUsers = Message.EndOfUsers(serverIdentity, message.channel, client.getNick())

        return listOf(
            CommandOutput(channelClients, proxiedJoin),
            CommandOutput(client, listOf(usersMsg, endOfUsers))
        )
    }

    fun handlePart(client: Client, message: Message.Part): List<CommandOutput> {
        val channel = state.getChannel(message.channel) ?: return emptyList()
        val channelClients = channel.getClients().toList()
        channel.removeClient(client)

        if (channel.getClients().isEmpty()) {
            state.removeChannel(channel)
        }

        val proxiedPart = Message.Part(client.getFullmask(), message.channel)
        return listOf(CommandOutput(channelClients, proxiedPart))
    }

    fun handlePrivmsg(client: Client, message: Message.Privmsg): List<CommandOutput> {
        val channel = state.getChannel(message.target)
        return if (channel == null) {
            val targetClient = state.getClient(message.target) ?: return emptyList()
            val privMsg = Message.Privmsg(client.getFullmask(), message.target, message.content)
            listOf(CommandOutput(targetClient, privMsg))
        } else {
            val privMsg = Message.Privmsg(client.getFullmask(), message.target, message.content)
            listOf(CommandOutput(channel.getClients().filter { it != client }, privMsg))
        }
    }

    fun handlePing(client: Client, message: Message.Ping): List<CommandOutput> {
        val pong = Message.Pong(serverIdentity, message.id)
        return listOf(CommandOutput(client, pong))
    }

    fun handleCap(client: Client, message: Message.Cap): List<CommandOutput> {
        return if (message.subcommand == "LS") {
            val cap = Message.Cap(serverIdentity, "LS", emptyList())
            listOf(CommandOutput(client, cap))
        } else if (message.subcommand == "REQ") {
            val nak = Message.Cap(serverIdentity, "NAK", emptyList())
            listOf(CommandOutput(client, nak))
        } else {
            emptyList()
        }
    }
}