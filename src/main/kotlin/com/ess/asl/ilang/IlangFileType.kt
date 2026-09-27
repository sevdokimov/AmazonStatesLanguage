package com.ess.asl.ilang

import com.ess.asl.ASLIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

object IlangFileType : LanguageFileType(AslIntrinsicLanguage.INSTANCE) {
    override fun getName(): @NonNls String = "ASL Intrinsic Language File"

    override fun getDescription(): @NlsContexts.Label String = "ASL intrinsic language file"

    override fun getDefaultExtension(): @NlsSafe String = "asl_ilang"

    override fun getIcon(): Icon = ASLIcons.FILE_TYPE

}
