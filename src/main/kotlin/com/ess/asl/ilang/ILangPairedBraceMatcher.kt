package com.ess.asl.ilang

import com.ess.asl.ilang.psi.ILangTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class ILangPairedBraceMatcher : PairedBraceMatcher {

    private val PAIRS = arrayOf(
        BracePair(
            ILangTypes.LPAREN,
            ILangTypes.RPAREN,
            false
        )
    )

    override fun getPairs(): Array<out BracePair?> = PAIRS

    override fun isPairedBracesAllowedBeforeType(
        p0: IElementType,
        p1: IElementType?
    ): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}