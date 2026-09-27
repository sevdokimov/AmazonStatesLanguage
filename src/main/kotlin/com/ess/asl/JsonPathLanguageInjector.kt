package com.ess.asl

import com.ess.asl.ilang.AslIntrinsicLanguage
import com.ess.asl.ilang.ILangLexerAdapter
import com.ess.asl.ilang.psi.ILangTypes
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.jsonpath.JsonPathLanguage
import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiLanguageInjectionHost
import java.util.stream.Collectors

class JsonPathLanguageInjector(private val supportDoubleDollar: Boolean, private val isReferencePath: Boolean = false) : StringValueHandler() {

    private fun injectForHost(registrar: MultiHostRegistrar, host: JsonStringLiteral, language: Language) {
        val fragments = host.textFragments
        if (fragments.isEmpty()) return

        registrar.startInjecting(language)
        for (fragment in fragments) {
            registrar.addPlace("", "", host as PsiLanguageInjectionHost, fragment.first!!)
        }

        registrar.doneInjecting()
    }

    private fun injectForHostSkipPrefix(registrar: MultiHostRegistrar, host: JsonStringLiteral, language: Language, prefixToSkip: String) {
        val fragments = host.textFragments
        if (fragments.isEmpty()) return

        assert(fragments[0].second.startsWith(prefixToSkip))

        registrar.startInjecting(language)

        var firstTextRange = fragments[0].first
        firstTextRange = TextRange(firstTextRange.startOffset + prefixToSkip.length, firstTextRange.endOffset)
        registrar.addPlace("", "", host as PsiLanguageInjectionHost, firstTextRange)

        for (i in 1 until fragments.size) {
            registrar.addPlace("", "", host as PsiLanguageInjectionHost, fragments[i].first)
        }

        registrar.doneInjecting()
    }

    private fun injectIntrinsicFunction(registrar: MultiHostRegistrar, host: JsonStringLiteral) {
        val fragments = host.textFragments
        if (fragments.isEmpty()) return

        val holeString = fragments.stream().map { it.second }.filter { it != null }.collect(Collectors.joining())
        val flags = BooleanArray(holeString.length)

        val lexer = ILangLexerAdapter()
        lexer.start(holeString)

        while (lexer.tokenType != null) {
            val inPath: Boolean = lexer.tokenType == ILangTypes.PATH_ITEM

            for (i in lexer.tokenStart until lexer.tokenEnd) {
                flags[i] = inPath
            }

            lexer.advance()
        }

        doInject(registrar, host, fragments, flags, false)
        doInject(registrar, host, fragments, flags, true)
    }

    fun doInject(
        registrar: MultiHostRegistrar,
        host: JsonStringLiteral,
        fragments: List<Pair<TextRange, String>>,
        flags: BooleanArray,
        isJsonPathLanguage: Boolean
    ) {
        var injectionStarted = false

        var prevFragmentsLen = 0
        for (f in fragments) {
            var firstFlag = flags[prevFragmentsLen]
            var partStart = 0

            for (i in 1 until f.second.length) {
                if (firstFlag != flags[prevFragmentsLen + i]) {
                    assert(f.first.length == f.second.length)

                    if (firstFlag == isJsonPathLanguage) {
                        if (!injectionStarted) {
                            registrar.startInjecting(if (isJsonPathLanguage) JsonPathLanguage.INSTANCE else AslIntrinsicLanguage.INSTANCE)
                            injectionStarted = true
                        }

                        val range = TextRange(f.first.startOffset + partStart, f.first.startOffset + i)
                        registrar.addPlace("", if (isJsonPathLanguage) "" else "1",
                        host as PsiLanguageInjectionHost, range)

                        if (isJsonPathLanguage) {
                            registrar.doneInjecting()
                            injectionStarted = false
                        }
                    }

                    firstFlag = flags[prevFragmentsLen + i]
                    partStart = i
                }
            }

            if (firstFlag == isJsonPathLanguage) {
                if (!injectionStarted) {
                    registrar.startInjecting(if (isJsonPathLanguage) JsonPathLanguage.INSTANCE else AslIntrinsicLanguage.INSTANCE)
                    injectionStarted = true
                }

                val range: TextRange

                if (f.first.length == 2 && f.second.length == 1) { // Is JSON escaping
                    assert(partStart == 0)
                    range = TextRange(f.first.startOffset + 1, f.first.endOffset)
                } else {
                    assert(f.first.length == f.second.length)
                    range = TextRange(f.first.startOffset + partStart, f.first.endOffset)
                }

                registrar.addPlace("", "", host as PsiLanguageInjectionHost, range)
            }

            prevFragmentsLen += f.second.length
        }

        if (injectionStarted) {
            registrar.doneInjecting()
        }
    }

    override fun injectLanguage(registrar: MultiHostRegistrar, context: JsonStringLiteral) {
        val beginning = context.node.firstChildNode.chars

        if (!beginning.startsWith("\"$")) {
            injectIntrinsicFunction(registrar, context)
            return
        }

        if (supportDoubleDollar && beginning.startsWith("\"$$.")) {
            injectForHostSkipPrefix(registrar, context, JsonPathLanguage.INSTANCE, "$")
        } else {
            injectForHost(registrar, context, JsonPathLanguage.INSTANCE)
        }
    }

    companion object {
        val INSTANCE = JsonPathLanguageInjector(false)
        val INSTANCE_DD = JsonPathLanguageInjector(true)
        val REFERENCE_PATH = JsonPathLanguageInjector(false, true)
    }
}