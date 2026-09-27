package com.ess.asl

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext

fun interface Completer {

    fun complete(params: CompletionParameters, ctx: ProcessingContext, result: CompletionResultSet)
}