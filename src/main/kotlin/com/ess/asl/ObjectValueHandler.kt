package com.ess.asl

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.util.containers.PeekableIterator

class ObjectValueHandler(vararg propertyList: Field) : SchemaValueHandler() {

    val properties: Map<String, Field> = propertyList.associateBy {it.name}

    override fun createInsertHandler(): InsertHandler<LookupElement> {
        return TypedValueHandler.OBJECT.createInsertHandler()
    }

    private fun completePropName(
        language: Lazy<String>,
        obj: JsonObject,
        result: CompletionResultSet
    ) {
        val existProps = HashSet<String>()
        for (prop in obj.propertyList) {
            existProps.add(prop.name)
        }

        for ((propName, field) in properties) {
            if (field.skipCompletion || existProps.contains(propName)) continue

            val handler = field.getHandler(language) ?: continue

            var element = LookupElementBuilder.create(propName)

            handler.createInsertHandler()?.let { insertHandler -> element = element.withInsertHandler(insertHandler) }

            result.addElement(element)
        }
    }

    override fun getHandler(language: Lazy<String>, path: PeekableIterator<PsiElement>): SchemaValueHandler? {
        if (!path.hasNext()) return null
        val obj = path.next() as? JsonObject ?: return null
        if (!path.hasNext()) return null
        val prop = path.next() as? JsonProperty ?: return null
        if (!path.hasNext()) return null

        val propChild = path.peek()

        if (propChild === prop.nameElement) {
            return object : StringValueHandler() {
                override fun complete(params: CompletionParameters, ctx: ProcessingContext, result: CompletionResultSet) {
                    completePropName(language, obj, result)
                }
            }
        }

        val field = properties[prop.name] ?: return null

        return field.getHandler(language)?.getHandler(language, path)
    }
}