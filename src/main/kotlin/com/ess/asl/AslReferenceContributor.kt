package com.ess.asl

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class AslReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(JsonStringLiteral::class.java).inFile(AslUtils.filePattern),

            object : PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference?> {
                return provideReferences(element as JsonStringLiteral)
            }
        })
    }

    private fun provideReferences(literal: JsonStringLiteral): Array<out PsiReference?> {
        val property = literal.parent as? JsonProperty ?: return emptyArray()

        if (property.value === literal) {
            val propName = property.name
            if (propName == "StartAt" && (property.parent as? JsonObject)?.parent is JsonFile) {
                return arrayOf(StateNameReference(literal))
            }

            val valueHandler = StateDefinitionObjectHandler.getValueHandler(literal) as? StringValueHandler ?: return emptyArray()
            valueHandler.getReference(literal).let { reference -> return arrayOf(reference) }
        }

        return emptyArray()
    }

}