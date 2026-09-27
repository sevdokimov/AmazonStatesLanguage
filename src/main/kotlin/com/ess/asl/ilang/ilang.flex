package com.ess.asl.ilang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.ess.asl.ilang.psi.ILangTypes.*;

%%

%class ILangLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%{

%}

%xstate IN_JSONPATH
%xstate IN_JSONPATH_EXPR


/* ---------- Character classes ---------- */

WHITE_SPACE = [ \t\r\n]+

DIGIT       = [0-9]
IDENTIFIER  = [A-Za-z_] [A-Za-z0-9_.]*

INTEGER     = -?{DIGIT}+
FLOAT       = -? (0 | [1-9][0-9]*) ( \. [0-9]+ )? ( [eE] [+-]? [0-9]+ )?

SQ_STRING   = \'([^\\\']|\\.)*\'
DQ_STRING   = \"([^\\\"]|\\.)*\"


JP_ESCAPE_SEQUENCE=\\[^\r\n]
JP_SINGLE_QUOTED_STRING=\'([^\\\'\r\n]|{JP_ESCAPE_SEQUENCE})*(\'|\\)?
JP_DOUBLE_QUOTED_STRING=\"([^\\\"\r\n]|{JP_ESCAPE_SEQUENCE})*(\"|\\)?


/* ---------- Lexer rules ---------- */

%%

<YYINITIAL> {
    {WHITE_SPACE}              { return TokenType.WHITE_SPACE; }
    {FLOAT}                    { return NUMBER; }
    {INTEGER}                  { return NUMBER; }

    //"true"                     { return TRUE; }
    //"false"                    { return FALSE; }
    "null"                     { return NULL; }

    "("                        { return LPAREN; }
    ")"                        { return RPAREN; }
    ","                        { return COMMA; }

    {SQ_STRING}                { return STRING_LITERAL; }
    //{DQ_STRING}                { return STRING_LITERAL; }

    /* identifiers */

    {IDENTIFIER}               { return IDENTIFIER; }

    "\$" "\$"? { yybegin(IN_JSONPATH); return PATH_ITEM; }
}

<IN_JSONPATH> {
    "." "."? [0-9a-zA-Z_]+   { return PATH_ITEM; }
    ".*"?  { return PATH_ITEM; }
    "."? "."? "[" {WHITE_SPACE}? (
            "*"
            | {JP_SINGLE_QUOTED_STRING}
            | {JP_DOUBLE_QUOTED_STRING}
            | {INTEGER} ({WHITE_SPACE}? "," {WHITE_SPACE}? {INTEGER})*
            | {INTEGER} ":" {INTEGER}?
            | ":" {INTEGER}
            | "-" {INTEGER} ":"
          )
        {WHITE_SPACE}?
    "]" { return PATH_ITEM; }

    "."? "."? "[" {WHITE_SPACE}? "?"? {WHITE_SPACE}? "(" {
           yybegin(IN_JSONPATH_EXPR); return PATH_ITEM;
    }

    "," { yybegin(YYINITIAL); return COMMA; }
    ")" { yybegin(YYINITIAL); return RPAREN; }
    {WHITE_SPACE} { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

    .  { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}

<IN_JSONPATH_EXPR> {
    {JP_SINGLE_QUOTED_STRING} | {JP_DOUBLE_QUOTED_STRING} | {WHITE_SPACE}? | [^ ")" ]+ { return PATH_ITEM; }
    ")" {WHITE_SPACE}? "]"  { yybegin(IN_JSONPATH); return PATH_ITEM; }
    ")"  { return PATH_ITEM; }
}

/* fallback */

.  { return TokenType.BAD_CHARACTER; }