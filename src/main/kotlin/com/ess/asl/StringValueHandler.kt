package com.ess.asl

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.PeekableIterator
import com.jetbrains.jsonSchema.impl.JsonOriginalPsiWalker
import com.jetbrains.jsonSchema.impl.JsonSchemaCompletionContributor
import com.jetbrains.jsonSchema.impl.JsonSchemaType

open class StringValueHandler(private val startCompletion: Boolean = false) : SchemaValueHandler() {

    open fun complete(params: CompletionParameters, ctx: ProcessingContext, result: CompletionResultSet) {

    }

    open fun injectLanguage(registrar: MultiHostRegistrar, context: JsonStringLiteral) {

    }

    open fun getReference(literal: JsonStringLiteral): PsiReference? {
        return null
    }

    override fun createInsertHandler(): InsertHandler<LookupElement>? {
        val insertHandler = JsonSchemaCompletionContributor.createPropertyInsertHandler(
            JsonSchemaType._string, null, null,
            JsonOriginalPsiWalker(), true, null
        )

        if (startCompletion) {
            return InsertHandler<LookupElement> { ctx, lookupElement ->
                insertHandler.handleInsert(ctx, lookupElement)
                AutoPopupController.getInstance(ctx.project).scheduleAutoPopup(ctx.editor)
            }
        }

        return insertHandler
    }

    override fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler? {
        return if (path.hasNext() && path.next() is JsonStringLiteral) this else null
    }

    companion object {

        val INSTANCE = StringValueHandler()

        fun of(completer: Completer): StringValueHandler {
            return object : StringValueHandler(true) {
                override fun complete(params: CompletionParameters, ctx: ProcessingContext, result: CompletionResultSet) {
                    completer.complete(params, ctx, result)
                }
            }
        }

        fun of(vararg values: String): StringValueHandler {
            return of { _, _, result ->
                for (value in values) {
                    result.addElement(LookupElementBuilder.create(value))
                }
            }
        }
    }
}