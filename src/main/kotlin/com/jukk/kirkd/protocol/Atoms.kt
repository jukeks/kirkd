package com.jukk.kirkd.protocol

data class Atoms(
    val prefix: String,
    val command: String,
    val params: List<String> = emptyList(),
    val trailing: String = "",
) {
    override fun toString(): String {
        if (prefix.isNotEmpty()) {
            return "Atoms(prefix='$prefix', command='$command', params=$params), trailing='$trailing'"
        }
        return "Atoms(command='$command', params=$params), trailing='$trailing'"
    }

    fun serialize(): String {
        val prefix = if (this.prefix.isNotEmpty()) ":$prefix " else ""
        return StringBuilder(prefix).apply {
            append(command)
            for (param in params) {
                append(" $param")
            }

            if (trailing.isNotEmpty()) {
                append(" :$trailing")
            }

            append("\r\n")
        }.toString()
    }
}