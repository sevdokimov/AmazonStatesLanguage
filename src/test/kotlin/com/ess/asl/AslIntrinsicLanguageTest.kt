package com.ess.asl

import com.ess.asl.ilang.IlangFileType
import com.intellij.psi.impl.DebugUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AslIntrinsicLanguageTest : BasePlatformTestCase() {

    fun testParamTypes() {
        validate("foo.bar()", """
        ILangFile
          ILangMethodCallImpl(METHOD_CALL)
            PsiElement(IDENTIFIER)('foo.bar')
            PsiElement(LPAREN)('(')
            PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo.bar('xxx')", """
        ILangFile
          ILangMethodCallImpl(METHOD_CALL)
            PsiElement(IDENTIFIER)('foo.bar')
            PsiElement(LPAREN)('(')
            ILangParamImpl(PARAM)
              PsiElement(STRING_LITERAL)(''xxx'')
            PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo.bar('x\\'x \\\\x')", """
        ILangFile
          ILangMethodCallImpl(METHOD_CALL)
            PsiElement(IDENTIFIER)('foo.bar')
            PsiElement(LPAREN)('(')
            ILangParamImpl(PARAM)
              PsiElement(STRING_LITERAL)(''x\'x \\x'')
            PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo.bar(1, 0, -10, 1.555 , 0.44e+5, 44.2E-10, 'r', null )", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo.bar')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      PsiElement(NUMBER)('1')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(NUMBER)('0')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(NUMBER)('-10')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(NUMBER)('1.555')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(NUMBER)('0.44e+5')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(NUMBER)('44.2E-10')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(STRING_LITERAL)(''r'')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(NULL)('null')
    PsiElement(RPAREN)(')')
        """.trimIndent())
    }

    fun testRecursiveCalls() {
        validate("foo.bar(xxx(yyy(1)), '555')", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo.bar')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangMethodCallImpl(METHOD_CALL)
        PsiElement(IDENTIFIER)('xxx')
        PsiElement(LPAREN)('(')
        ILangParamImpl(PARAM)
          ILangMethodCallImpl(METHOD_CALL)
            PsiElement(IDENTIFIER)('yyy')
            PsiElement(LPAREN)('(')
            ILangParamImpl(PARAM)
              PsiElement(NUMBER)('1')
            PsiElement(RPAREN)(')')
        PsiElement(RPAREN)(')')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(STRING_LITERAL)(''555'')
    PsiElement(RPAREN)(')')
        """.trimIndent())
    }

    fun testJsonPath() {
        validate("foo($.aaa)", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.aaa')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($.aaa['property'])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.aaa')
        PsiElement(PATH_ITEM)('['property']')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($[  \"property\"  ])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('[  "property"  ]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($.[  \"property\"  ])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.[  "property"  ]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($..[  \"property\"  ])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('..[  "property"  ]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($[ 10])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('[ 10]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($[0, 10])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('[0, 10]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($..aaa)", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('..aaa')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($.aaa.*)", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.aaa')
        PsiElement(PATH_ITEM)('.*')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($.aaa.[ * ])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.aaa')
        PsiElement(PATH_ITEM)('.[ * ]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($[ 1:100 ])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('[ 1:100 ]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($[1100: ])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('[1100: ]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($[:5])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('[:5]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($[-50:])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('[-50:]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($.aaa[?(@.x > 10)])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.aaa')
        PsiElement(PATH_ITEM)('[?(')
        PsiElement(PATH_ITEM)('@.x')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('>')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('10')
        PsiElement(PATH_ITEM)(')]')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($.aaa[  ? (  @.x > 10 ) ] , 100)", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.aaa')
        PsiElement(PATH_ITEM)('[  ? (')
        PsiElement(PATH_ITEM)('  ')
        PsiElement(PATH_ITEM)('@.x')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('>')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('10')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)(') ]')
    PsiElement(COMMA)(',')
    ILangParamImpl(PARAM)
      PsiElement(NUMBER)('100')
    PsiElement(RPAREN)(')')
        """.trimIndent())

        validate("foo($.aaa[?((@.x > 10) && z)])", """
ILangFile
  ILangMethodCallImpl(METHOD_CALL)
    PsiElement(IDENTIFIER)('foo')
    PsiElement(LPAREN)('(')
    ILangParamImpl(PARAM)
      ILangPathImpl(PATH)
        PsiElement(PATH_ITEM)('$')
        PsiElement(PATH_ITEM)('.aaa')
        PsiElement(PATH_ITEM)('[?(')
        PsiElement(PATH_ITEM)('(@.x')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('>')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('10')
        PsiElement(PATH_ITEM)(')')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('&&')
        PsiElement(PATH_ITEM)(' ')
        PsiElement(PATH_ITEM)('z')
        PsiElement(PATH_ITEM)(')]')
    PsiElement(RPAREN)(')')
        """.trimIndent())
    }

    fun validate(text: String, expected: String) {
        val file = myFixture.configureByText(IlangFileType, text)
        assertEquals(expected, DebugUtil.psiToString(file, false).trimEnd())
    }

}