package com.jukk.kirkd.server

import com.jukk.kirkd.client.Client

data class Channel(val name: String, private val clients: MutableList<Client>) {
    fun addClient(client: Client) {
        if (clients.contains(client)) {
            return
        }
        clients.add(client)
    }

    fun removeClient(client: Client) {
        clients.remove(client)
    }

    fun getClients(): List<Client> {
        return clients
    }
}

class State {
    private val clients = mutableListOf<Client>()
    private val channels = mutableMapOf<String, Channel>()

    fun addClient(client: Client) {
        clients.add(client)
    }

    fun removeClient(client: Client) {
        clients.remove(client)

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