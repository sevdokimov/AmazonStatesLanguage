package com.ess.asl

import com.intellij.json.JsonLanguage
import com.intellij.lang.Language
import cz.tix.jsonata.lang.JsonataLanguage

class JSONanaLanguageInjectorTest : AslTestBase() {

    private fun checkLangInjection(property: String, expectedLang: Language, fileContent: String? = null) {
        configure(
            """
            {
                "StartAt": "DefaultState",
                "States": {
                    "FirstMatchState": {
                        "Type" : "Task",
                        "QueryLanguage": "JSONata",
                        "Output": "$property"
                    }
                }
            }
            """)

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertEquals(expectedLang, element!!.language)

        if (fileContent != null)
            assertEquals(fileContent, element.containingFile.text)
    }

    fun testJsonataInjection() {
        checkLangInjection("<caret>", JsonLanguage.INSTANCE)
        checkLangInjection("aa<caret>aa", JsonLanguage.INSTANCE)
        checkLangInjection("{% aa<caret>aa", JsonLanguage.INSTANCE)
        checkLangInjection("{%<caret>%}", JsonLanguage.INSTANCE)
        checkLangInjection("{% \$states.<caret>result %}", JsonataLanguage, " \$states.result ")
        checkLangInjection("{% \$approvalResult.decision<caret> = 'a\\\"a' %}", JsonataLanguage, " \$approvalResult.decision = 'a\"a' ")
    }

    fun testGlobalLangDefinition() {
        configure(
            """
            {
                "StartAt": "DefaultState",
                "QueryLanguage": "JSONata",
                "States": {
                    "FirstMatchState": {
                        "Type" : "Task",
                        "Output": "{% ${'$'}states.<caret>result %}"
                    }
                }
            }
            """)

        val element = myFixture.file.findElementAt(myFixture.caretOffset)
        assertEquals(JsonataLanguage, element!!.language)
    }

}