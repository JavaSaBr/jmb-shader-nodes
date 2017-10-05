package com.jme3.shader.glsl.parser.ast.util;

import com.jme3.shader.glsl.parser.GLSLLang;
import com.jme3.shader.glsl.parser.GLSLParser;
import com.jme3.shader.glsl.parser.Token;
import com.jme3.shader.glsl.parser.ast.ASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.MethodDeclarationASTNode;

import java.util.List;

/**
 * The utility class to work with AST nodes.
 *
 * @author JavaSaBr
 */
public class ASTUtils {

    public static final Predicate<Token> EMPTY = new Predicate<Token>() {
        @Override
        public boolean test(final Token token) {
            return false;
        }
    };

    public static final Predicate<Token> END_IF = new Predicate<Token>() {
        @Override
        public boolean test(final Token token) {
            return token.getType() == GLSLParser.TOKEN_DEFINE && token.getText().equals(GLSLLang.PR_ENDIF);
        }
    };

    public static final Predicate<Token> END_IF_OR_ELSE_OR_ELSE_IF = new Predicate<Token>() {
        @Override
        public boolean test(final Token token) {

            if (token.getType() != GLSLParser.TOKEN_DEFINE) {
                return false;
            }

            final String text = token.getText();
            return text.equals(GLSLLang.PR_ENDIF) || text.equals(GLSLLang.PR_ELSE) || text.equals(GLSLLang.PR_ELIF);
        }
    };

    public static final Predicate<Token> RIGHT_BRACE = new Predicate<Token>() {
        @Override
        public boolean test(final Token token) {
            return token.getType() == GLSLParser.TOKEN_RIGHT_BRACE;
        }
    };

    /**
     * Get parse level of the node.
     *
     * @param node the node.
     * @return the parse level.
     */
    public static int getParseLevel(final ASTNode node) {

        if (hasParentByType(node, MethodDeclarationASTNode.class)) {
            return GLSLParser.LEVEL_METHOD;
        }

        return GLSLParser.LEVEL_FILE;
    }

    /**
     * Checks of existing a parent of the type.
     *
     * @param node the node.
     * @param type the type of a parent.
     * @return true if a parent of the type is exists.
     */
    public static boolean hasParentByType(final ASTNode node, final Class<? extends ASTNode> type) {

        ASTNode parent = node.getParent();
        while (parent != null) {
            if (type.isInstance(parent)) {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }

    public static void updateOffsetAndLengthAndText(final ASTNode node, final char[] content) {

        final List<ASTNode> children = node.getChildren();
        if (children.isEmpty()) {
            node.setOffset(0);
            updateLengthAndText(node, content);
            return;
        }

        final ASTNode first = children.get(0);
        node.setOffset(first.getOffset());

        updateLengthAndText(node, content);
    }

    public static void updateLengthAndText(final ASTNode node, final char[] content) {

        final List<ASTNode> children = node.getChildren();
        if (children.isEmpty()) {
            node.setLength(0);
            node.setText("");
            return;
        }

        final ASTNode last = children.get(children.size() - 1);
        node.setLength(last.getOffset() + last.getLength() - node.getOffset());

        updateText(node, content);
    }

    public static void updateText(final ASTNode node, final char[] content) {
        node.setText(String.valueOf(content, node.getOffset(), node.getLength()));
    }

    public static String getIndent(final ASTNode node) {

        int count = 0;

        ASTNode parent = node.getParent();
        while (parent != null) {
            count++;
            parent = parent.getParent();
        }

        return getIndent(count);
    }

    public static String getIndent(final int level) {

        final char[] result = new char[level * 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = ' ';
        }

        return String.valueOf(result);
    }
}
