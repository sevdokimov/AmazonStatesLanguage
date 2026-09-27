package com.ess.asl

import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import cz.tix.jsonata.lang.JsonataLanguage

private const val JSONATA_START_MARKER = "\"{%"
private const val JSONATA_END_MARKER = "%}\""

object JSONanaLanguageInjector {

    fun isLanguageJSONata(context: JsonStringLiteral): Boolean {
        var obj = context.parent
        var prev1: PsiElement? = null
        var prev2: PsiElement? = null
        var prev3: PsiElement? = null

        while (obj != null) {
            if (obj is JsonProperty && obj.name == "States"
                && (obj.parent as? JsonObject)?.findProperty("StartAt") != null) {
                val stateObj = prev3 as? JsonObject ?: return false
                stateObj.findProperty("QueryLanguage")?.let { return (it.value as? JsonStringLiteral)?.value == AslUtils.JSONata }

                val root = obj.parent as? JsonObject ?: return false
                return (root.findProperty("QueryLanguage")?.value as? JsonStringLiteral)?.value == AslUtils.JSONata
            }

            prev3 = prev2
            prev2 = prev1
            prev1 = obj
            obj = obj.parent
        }

        return false
    }

    fun injectLanguage(
        registrar: MultiHostRegistrar,
        context: JsonStringLiteral
    ): Boolean {
        val beginning = context.node.firstChildNode.chars
        if (!beginning.startsWith(JSONATA_START_MARKER)) return false

        val end = context.node.lastChildNode.chars
        if (!end.endsWith(JSONATA_END_MARKER)) return false

        if (context.textLength <= JSONATA_START_MARKER.length + JSONATA_END_MARKER.length)
            return false // empty string

        if (!isLanguageJSONata(context)) return false

        registrar.startInjecting(JsonataLanguage)

        val textFragments = context.textFragments

        val firstFragment = textFragments.first()
        val lastFragment = textFragments.last()
        assert(firstFragment.first.length == firstFragment.second.length)
        assert(firstFragment.second.startsWith("{%"))

        if (firstFragment === lastFragment) {
            registrar.addPlace("", "", context as PsiLanguageInjectionHost,
                TextRange(firstFragment.first.startOffset + "{%".length, firstFragment.first.endOffset - "%}".length))
        } else {
            registrar.addPlace("", "", context as PsiLanguageInjectionHost,
                TextRange(firstFragment.first.startOffset + "{%".length, firstFragment.first.endOffset))

            for (i in 1 until textFragments.size - 1) {
                val f = textFragments[i]

                val range = if (f.first.length == 2 && f.second.length == 1) {
                    // JSON escaping
                    TextRange(f.first.startOffset + 1, f.first.endOffset)
                } else {
                    assert(f.first.length == f.second.length)
                    f.first
                }

                registrar.addPlace("", "", context as PsiLanguageInjectionHost, range)
            }

            assert(lastFragment.first.length == lastFragment.second.length)
            assert(lastFragment.second.endsWith("%}"))

            if (lastFragment.second.length > "%}".length) {
                registrar.addPlace("", "", context as PsiLanguageInjectionHost,
                    TextRange(lastFragment.first.startOffset, lastFragment.first.endOffset - "%}".length))
            }
        }

        registrar.doneInjecting()
        return true
    }
}