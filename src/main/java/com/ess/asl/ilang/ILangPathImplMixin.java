package com.ess.asl.ilang;

import com.ess.asl.ilang.psi.ILangPath;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

public class ILangPathImplMixin extends ASTWrapperPsiElement implements ILangPath, PsiLanguageInjectionHost {

    protected ILangPathImplMixin(ASTNode node) {
        super(node);
    }


    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String s) {
        PsiElement firstChild = getFirstChild();
        assert firstChild instanceof LeafPsiElement;

        while (true) {
            PsiElement sibling = firstChild.getNextSibling();
            if (sibling == null)
                break;

            sibling.delete();
        }

        ((LeafPsiElement) firstChild).replaceWithText(s);
        return this;
    }

    @Override
    public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new LiteralTextEscaper<>(this) {
            @Override
            public boolean decode(TextRange rangeInsideHost, StringBuilder outChars) {
                outChars.append(rangeInsideHost.subSequence(myHost.getText()));
                return true;
            }

            @Override
            public int getOffsetInHost(int i, @NotNull TextRange textRange) {
                return i + textRange.getStartOffset();
            }

            @Override
            public boolean isOneLine() {
                return true;
            }
        };
    }
}
