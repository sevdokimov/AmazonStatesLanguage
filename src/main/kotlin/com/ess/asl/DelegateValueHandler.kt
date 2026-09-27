package com.ess.asl

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.util.containers.PeekableIterator

class DelegateValueHandler() : SchemaValueHandler() {

    var delegate: SchemaValueHandler? = null

    override fun createInsertHandler(): InsertHandler<LookupElement>? {
        return delegate!!.createInsertHandler()
    }

    override fun getHandler(
        language: Lazy<String>,
        path: PeekableIterator<PsiElement>
    ): SchemaValueHandler? {
        return delegate!!.getHandler(language, path)
    }
}