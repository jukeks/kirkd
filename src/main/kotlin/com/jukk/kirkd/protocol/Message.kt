package com.jukk.kirkd.protocol

open class Message private constructor() {
    class User(val user: String, val host: String, val servername: String, val realName: String) : Message()
    class Privmsg(val prefix: String, val target: String, val content: String) : Message()
    class Join(val prefix: String, val channel: String) : Message()
    class Part(val prefix: String, val channel: String, val message: String?) : Message()
    class Ping(val prefix: String, val id: String) : Message()
    class Pong(val prefix: String, val id: String) : Message()
    class Quit(val prefix: String, val message: String) : Message()
    class Nick(val prefix: String, val nick: String) : Message()
    class Topic(val prefix: String, val channel: String, val topic: String) : Message()
    class TopicReply(val prefix: String, val channel: String, val nick: String, val topic: String) : Message()
    class Users(val prefix: String, val channel: String, val nick: String, val users: List<String>) : Message()
    class EndOfUsers(val prefix: String, val channel: String, val nick: String) : Message()
    class EndOfMotd(val prefix: String, val nick: String) : Message()
    class Unknown(val prefix: String, val command: String, val params: List<String>) : Message()
    class Cap(val prefix: String, val subcommand: String, val params: List<String>) : Message()
    class Welcome(val prefix: String, val nick: String) : Message()
    class NickInUse(val prefix: String, val nick: String?, val newNick: String) : Message()
    class RegisterFirst(val prefix: String, val nick: String?) : Message()
}