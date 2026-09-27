package com.ess.asl.ilang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class ILangFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, AslIntrinsicLanguage.INSTANCE) {
    override fun getFileType(): FileType = IlangFileType

    override fun toString(): String = "ILangFile"
}