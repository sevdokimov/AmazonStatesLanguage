package com.ess.asl

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement
import com.intellij.util.containers.PeekableIterator

class ParallelStateValueHandler : SchemaValueHandler() {

    override fun createInsertHandler(): InsertHandler<LookupElement> {
        return TypedValueHandler.OBJECT.createInsertHandler()
    }

    override fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler? {
        return StateDefinitionObjectHandler.INSTANCE.getHandler(language, path)
    }
}