package com.ess.asl

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.util.containers.PeekableIterator

abstract class SchemaValueHandler {

    open fun createInsertHandler(): InsertHandler<LookupElement>? {
        return null
    }

    abstract fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler?

}