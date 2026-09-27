package com.ess.asl

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.util.containers.PeekableIterator
import com.jetbrains.jsonSchema.impl.JsonOriginalPsiWalker
import com.jetbrains.jsonSchema.impl.JsonSchemaCompletionContributor
import com.jetbrains.jsonSchema.impl.JsonSchemaType

class TypedValueHandler(private val type: JsonSchemaType, private val defaultValue: String? = null) : SchemaValueHandler() {

    override fun createInsertHandler(): InsertHandler<LookupElement> {
        return JsonSchemaCompletionContributor.createPropertyInsertHandler(
            type, defaultValue, null,
            JsonOriginalPsiWalker(), true, null
        )
    }

    override fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler {
        return this
    }

    companion object {
        val OBJECT = TypedValueHandler(JsonSchemaType._object)
        val ARRAY = TypedValueHandler(JsonSchemaType._array)
        val INT = TypedValueHandler(JsonSchemaType._integer)
        val BOOL = TypedValueHandler(JsonSchemaType._boolean)

        val ANY = TypedValueHandler(JsonSchemaType._any)
    }
}