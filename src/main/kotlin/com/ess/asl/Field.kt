package com.ess.asl

class Field(val name: String, private val jsonPathHandler: SchemaValueHandler?, private val jsonataHandler: SchemaValueHandler?, val skipCompletion: Boolean = false) {

    init {
        assert(jsonPathHandler != null || jsonataHandler != null)
    }

    fun getHandler(language: Lazy<String>): SchemaValueHandler? {
        if (jsonPathHandler === jsonataHandler)
            return jsonataHandler

        return if (language.value == AslUtils.JSONata) jsonataHandler else jsonPathHandler
    }

    fun skipCompletion(): Field {
        return Field(name, jsonPathHandler, jsonataHandler, true)
    }

    companion object {
        fun field(name: String, handler: SchemaValueHandler): Field {
            return Field(name, handler, handler)
        }

        fun field(name: String, jsonPathHandler: SchemaValueHandler, jsonataHandler: SchemaValueHandler): Field {
            return Field(name, jsonPathHandler, jsonataHandler)
        }

        fun fieldP(name: String, handler: SchemaValueHandler): Field {
            return Field(name, handler, null)
        }

        fun fieldN(name: String, handler: SchemaValueHandler): Field {
            return Field(name, null, handler)
        }
    }
}