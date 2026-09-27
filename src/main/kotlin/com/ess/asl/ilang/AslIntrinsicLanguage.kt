package com.ess.asl.ilang

import com.intellij.lang.InjectableLanguage
import com.intellij.lang.Language

class AslIntrinsicLanguage : Language("AslIntrinsic"), InjectableLanguage {

    companion object {
        val INSTANCE = AslIntrinsicLanguage()
    }
}