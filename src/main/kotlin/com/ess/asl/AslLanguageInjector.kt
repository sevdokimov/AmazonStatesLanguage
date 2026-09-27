package com.ess.asl

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.Unmodifiable

class AslLanguageInjector : MultiHostInjector {

    private val elementsToInject = listOf<Class<out PsiElement?>?>(JsonStringLiteral::class.java)

    override fun getLanguagesToInject(
        registrar: MultiHostRegistrar,
        context: PsiElement
    ) {
        if (context !is JsonStringLiteral) return

        if (!context.containingFile.name.endsWith(AslUtils.FILE_SUFFIX))
            return

        if (JSONanaLanguageInjector.injectLanguage(registrar, context)) return

        val valueHandler = StateDefinitionObjectHandler.getValueHandler(context) as? StringValueHandler ?: return
        valueHandler.injectLanguage(registrar, context)
    }

    override fun elementsToInjectIn(): @Unmodifiable List<Class<out PsiElement?>?> {
        return elementsToInject
    }
}