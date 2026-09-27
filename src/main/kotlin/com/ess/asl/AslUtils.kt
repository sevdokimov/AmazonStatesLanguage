package com.ess.asl

import com.intellij.openapi.util.Key
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns

object AslUtils {

    const val JSON_PATH = "JSONPath"
    const val JSONata = "JSONata"

    val REFERENCE_PATH_KEY = Key<Boolean>("AslUtils.REFERENCE_PATH")

    const val FILE_SUFFIX = ".asl.json"

    val filePattern = PlatformPatterns.psiFile().withName(StandardPatterns.string().endsWith(FILE_SUFFIX))
}