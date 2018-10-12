package com.template


import net.corda.core.serialization.SerializationWhitelist


class TemplateSerializationWhitelist: SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf()
}

