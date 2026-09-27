package com.ess.asl

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.json.psi.JsonArray
import com.intellij.psi.PsiElement
import com.intellij.util.containers.PeekableIterator

class ArrayValueHandler(private val itemHandler: SchemaValueHandler) : SchemaValueHandler() {

    override fun createInsertHandler(): InsertHandler<LookupElement> {
        return TypedValueHandler.ARRAY.createInsertHandler()
    }

    override fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler? {
        if (!path.hasNext()) return null

        if (path.next() !is JsonArray) return null

        return itemHandler.getHandler(language, path)
    }
}