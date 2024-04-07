package com.jukk.kirk.server

import com.jukk.kirk.client.Client

data class Channel(val name: String, private val clients: MutableList<Client>) {
    fun addClient(client: Client) {
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
        // todo remove from all channels
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