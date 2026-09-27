package com.ess.asl

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.util.containers.PeekableIterator
import com.jetbrains.jsonSchema.impl.JsonOriginalPsiWalker
import com.jetbrains.jsonSchema.impl.JsonSchemaCompletionContributor
import com.jetbrains.jsonSchema.impl.JsonSchemaType

class PayloadTemplateValueHandler : SchemaValueHandler() {

    override fun createInsertHandler(): InsertHandler<LookupElement> {
        return JsonSchemaCompletionContributor.createPropertyInsertHandler(
            JsonSchemaType._object, null, null,
            JsonOriginalPsiWalker(), true, null
        )
    }

    override fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler? {
        if (!path.hasNext()) return null

        var lastElement: PsiElement? = null

        while (path.hasNext()) {
            lastElement = path.next()
        }

        if (lastElement !is JsonStringLiteral) return null

        val property = lastElement.parent as? JsonProperty ?: return null
        if (property.value !== lastElement) return null
        
        if (property.name.endsWith(".$")) {
            return JsonPathLanguageInjector.INSTANCE_DD
        }

        return null
    }
}