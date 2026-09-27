package com.ess.asl

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiReference

class StateReferenceValueHandler : StringValueHandler(true) {

    override fun getReference(literal: JsonStringLiteral): PsiReference {
        return StateNameReference(literal)
    }
}