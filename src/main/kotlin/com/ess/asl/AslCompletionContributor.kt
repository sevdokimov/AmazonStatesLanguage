package com.ess.asl

import com.intellij.codeInsight.completion.*
import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.util.ProcessingContext

class AslCompletionContributor : CompletionContributor(), DumbAware {

    init {
        extend(
            CompletionType.BASIC,
            psiElement(JsonElementTypes.DOUBLE_QUOTED_STRING).inFile(AslUtils.filePattern),
            AslCompletionProvider()
        )
    }

    class AslCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(params: CompletionParameters, ctx: ProcessingContext, result: CompletionResultSet) {
            val literal = params.position.parent as? JsonStringLiteral  ?: return

            val valueHandler = StateDefinitionObjectHandler.getValueHandler(literal) as? StringValueHandler ?: return

            valueHandler.complete(params, ctx, result)
        }
    }

}