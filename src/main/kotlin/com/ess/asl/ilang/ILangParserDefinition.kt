package com.ess.asl.ilang

import com.ess.asl.ilang.psi.ILangTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class ILangParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer {
        return ILangLexerAdapter()
    }

    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY

    override fun getWhitespaceTokens(): TokenSet = TokenSet.WHITE_SPACE

    override fun getStringLiteralElements(): TokenSet = STR

    override fun createParser(project: Project): PsiParser {
        return ILangParser()
    }

    override fun getFileNodeType(): IFileElementType = FILE

    override fun createFile(fvProvider: FileViewProvider): PsiFile {
        return ILangFile(fvProvider)
    }

    override fun createElement(node: ASTNode?): PsiElement {
        return ILangTypes.Factory.createElement(node)
    }

    companion object {
        val FILE: IFileElementType = IFileElementType(AslIntrinsicLanguage.INSTANCE)

        val STR = TokenSet.create(ILangTypes.STRING_LITERAL)
    }
}