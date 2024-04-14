package com.jukk.kirkd.protocol

object Parser {
    private fun strippingPartition(input: String): Pair<String, String> {
        val components = input.split(" ", limit = 2)
        return Pair(components[0], components.getOrNull(1)?.trimStart() ?: "")
    }

    private fun parseParams(paramsRaw: String): List<String> {
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