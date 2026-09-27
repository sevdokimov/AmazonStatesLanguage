package com.ess.asl

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

class StateNameReference(literal: JsonStringLiteral) : PsiReferenceBase<JsonStringLiteral>(literal) {

    override fun resolve(): PsiElement? {
        val stateObj = findStateObject() ?: return null

        return stateObj.findProperty(element.value)
    }

    override fun getVariants(): Array<out Any?> {
        val stateObj = findStateObject() ?: return emptyArray<Any>()

        val currentStateName = getCurrentStateName(stateObj)

        val res = mutableListOf<Any>()

        for (property in stateObj.propertyList) {
            val name = property.name
            if (name != currentStateName) {
                res.add(LookupElementBuilder.create(name).withPsiElement(property.nameElement))
            }
        }
        
        return res.toTypedArray()
    }

    private fun getCurrentStateName(stateObj: JsonObject): String? {
        var e = element.parent

        while (e != null) {
            val parent = e.parent

            if (parent == stateObj)
                return (e as? JsonProperty)?.name

            e = parent
        }

        return null
    }

    private fun findStateObject(): JsonObject? {
        var e = element.parent

        while (true) {
            val parent = e.parent

            if (e is JsonProperty) {
                val name = e.name

                if (name == "StartAt") {
                    return (parent as? JsonObject)?.findProperty("States")?.value as? JsonObject
                }

                if (name == "States" && (parent as? JsonObject)?.findProperty("StartAt") != null) {
                    return e.value as? JsonObject
                }
            }

            e = parent
        }
    }
}