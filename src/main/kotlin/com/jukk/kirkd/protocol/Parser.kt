package com.jukk.kirkd.protocol

data class Atoms(
    val prefix: String,
    val command: String,
    val params: List<String>
) {
    override fun toString(): String {
        if (prefix.isNotEmpty()) {
            return "Atoms(prefix='$prefix', command='$command', params=$params)"
        }
        return "Atoms(command='$command', params=$params)"
    }
}

object Parser {
    fun strippingPartition(input: String): Pair<String, String> {
        val components = input.split(" ", limit = 2)
        return Pair(components[0], components.getOrNull(1)?.trimStart() ?: "")
    }

    fun parseParams(paramsRaw: String): List<String> {
        val params = mutableListOf<String>()
        var rest = paramsRaw
        while (rest.isNotEmpty()) {
            if (rest.startsWith(":")) {
                params.add(rest.substring(1))
                break
            }

            val (param, newRest) = strippingPartition(rest)
            params.add(param)
            rest = newRest
        }

        return params
    }

    fun atomsFromString(input: String): Atoms {
        if (input.startsWith(":")) {
            val (prefix, rest) = strippingPartition(input)
            val (command, params) = strippingPartition(rest)
            return Atoms(prefix, command, parseParams(params))
        }

        val prefix = ""
        val (command, params) = strippingPartition(input)
        return Atoms(prefix, command, parseParams(params))
    }
}