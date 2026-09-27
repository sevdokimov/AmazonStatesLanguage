package com.ess.asl.ilang

import com.ess.asl.ilang.psi.ILangTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType


object ILangSyntaxHighlighter : SyntaxHighlighterBase() {

    val keyString = arrayOf(DefaultLanguageHighlighterColors.STRING)
    val keyNumber = arrayOf(DefaultLanguageHighlighterColors.NUMBER)
    val keyNull = arrayOf(DefaultLanguageHighlighterColors.KEYWORD)
    val keyBrackets = arrayOf(DefaultLanguageHighlighterColors.BRACKETS)

    override fun getHighlightingLexer(): Lexer = ILangLexerAdapter()

    override fun getTokenHighlights(e: IElementType?): Array<out TextAttributesKey?> {
        if (e == ILangTypes.NUMBER) return keyNumber
        if (e == ILangTypes.STRING_LITERAL) return keyString
        if (e == ILangTypes.NULL) return keyNull
        if (e == ILangTypes.LPAREN || e == ILangTypes.RPAREN) return keyBrackets

        return TextAttributesKey.EMPTY_ARRAY
    }


}