package com.jukk.kirkd.server

import com.jukk.kirkd.client.Client

data class Channel(val name: String, private val clients: MutableSet<Client>) {
    fun addClient(client: Client) {
        clients.add(client)
    }

    fun removeClient(client: Client) {
        clients.remove(client)
    }

    fun getClients(): Set<Client> {
        return clients
    }
}

class NickInUseException : Exception()

class State {
    private val clients = mutableListOf<Client>()
    private val channels = mutableMapOf<String, Channel>()
    private val nicks = mutableSetOf<String>()

    fun addClient(client: Client) {
        clients.add(client)
    }

    fun addNewNick(nick: String): Result<Unit> {
        if (nicks.contains(nick)) {
            return Result.failure(NickInUseException())
        }

        nicks.add(nick)

        return Result.success(Unit)
    }

    fun changeNick(old: String, new: String): Result<Unit> {
        if (nicks.contains(new)) {
            return Result.failure(NickInUseException())
        }

        nicks.remove(old)
        nicks.add(new)

        return Result.success(Unit)
    }

    fun removeNick(nick: String) {
        nicks.remove(nick)
    }

    fun removeClient(client: Client) {
        clients.remove(client)
        removeNick(client.getNick())

        // most inefficient way to remove a client from all channels
        for ((_, channel) in channels) {
            channel.removeClient(client)
        }
    }

    fun getClient(target: String): Client? {
        return null
    }

    fun addChannel(channel: Channel) {
        channels[channel.name] = channel
    }

    fun removeChannel(channel: Channel) {
        channels.remove(channel.name)
    }

    fun getChannel(name: String): Channel? {
        return channels[name]
    }
}