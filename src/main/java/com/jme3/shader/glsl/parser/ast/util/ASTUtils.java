package com.jme3.shader.glsl.parser.ast.util;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.glsl.parser.GLSLLang;
import com.jme3.shader.glsl.parser.GLSLParser;
import com.jme3.shader.glsl.parser.Token;
import com.jme3.shader.glsl.parser.ast.ASTNode;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.ExternalFieldDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.ExternalFieldDeclarationASTNode.ExternalFieldType;
import com.jme3.shader.glsl.parser.ast.declaration.MethodDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.ExtensionPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.value.DefineValueASTNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The utility class to work with AST nodes.
 *
 * @author JavaSaBr
 */
public class ASTUtils {

    public static final BiPredicate<GLSLParser, char[]> EMPTY = new BiPredicate<GLSLParser, char[]>() {
        @Override
        public boolean test(final GLSLParser parser, final char[] content) {
            return false;
        }
    };

    public static final BiPredicate<GLSLParser, char[]> END_IF = new BiPredicate<GLSLParser, char[]>() {
        @Override
        public boolean test(final GLSLParser parser, final char[] content) {

            final Token token = parser.readToken(content);

            if (token.getType() != GLSLParser.TOKEN_PREPROCESSOR) {
                return false;
            }

            final Token keyWordToken = parser.findToken(content, GLSLParser.TOKEN_KEYWORD);
            final String text = keyWordToken.getText();
            return text.equals(GLSLLang.PR_TYPE_ENDIF);
        }
    };

    public static final BiPredicate<GLSLParser, char[]> END_IF_OR_ELSE_OR_ELSE_IF = new BiPredicate<GLSLParser, char[]>() {
        @Override
        public boolean test(final GLSLParser parser, final char[] content) {

            final Token token = parser.readToken(content);

            if (token.getType() != GLSLParser.TOKEN_PREPROCESSOR) {
                return false;
            }

            final Token keyWordToken = parser.findToken(content, GLSLParser.TOKEN_KEYWORD);
            final String text = keyWordToken.getText();
            return text.equals(GLSLLang.PR_TYPE_ENDIF) || text.equals(GLSLLang.PR_TYPE_ELSE) ||
                    text.equals(GLSLLang.PR_TYPE_ELIF);
        }
    };

    public static final BiPredicate<GLSLParser, char[]> RIGHT_BRACE = new BiPredicate<GLSLParser, char[]>() {
        @Override
        public boolean test(final GLSLParser parser, final char[] content) {
            final Token token = parser.readToken(content);
            return token.getType() == GLSLParser.TOKEN_RIGHT_BRACE;
        }
    };

    public static final BiPredicate<GLSLParser, char[]> IF_WITHOUT_BRACES = new BiPredicate<GLSLParser, char[]>() {
        @Override
        public boolean test(final GLSLParser parser, final char[] content) {

            Token token = parser.getPreviousReadToken();
            if (token.getType() == GLSLParser.TOKEN_SEMICOLON) {
                return true;
            }

            token = parser.readToken(content);
            return token.getType() == GLSLParser.TOKEN_SEMICOLON;
        }
    };

    private static final CharPredicate PREVIOUS_VAR_CHAR_CHECKER = new CharPredicate() {
        @Override
        public boolean test(final char value) {
            switch (value) {
                case ' ':
                case '\n':
                case '\r':
                case ',':
                case '=':
                case '+':
                case '*':
                case '-':
                case '/':
                case '(': {
                    return true;
                }
                default:
                    return false;
            }
        }
    };

    private static final CharPredicate NEXT_VAR_CHAR_CHECKER = new CharPredicate() {
        @Override
        public boolean test(final char value) {
            switch (value) {
                case ' ':
                case '\n':
                case '\r':
                case ',':
                case '=':
                case '.':
                case '+':
                case '*':
                case '-':
                case '/':
                case ';':
                case ')': {
                    return true;
                }
                default:
                    return false;
            }
        }
    };

    private static final CharPredicate PREVIOUS_METHOD_CHAR_CHECKER = new CharPredicate() {
        @Override
        public boolean test(final char value) {
            return PREVIOUS_VAR_CHAR_CHECKER.test(value);
        }
    };

    private static final CharPredicate NEXT_METHOD_CHAR_CHECKER = new CharPredicate() {
        @Override
        public boolean test(final char value) {
            switch (value) {
                case ' ':
                case '\n':
                case '\r':
                case ',':
                case '=':
                case '.':
                case '+':
                case '*':
                case '-':
                case '/':
                case ';':
                case '(': {
                    return true;
                }
                default:
                    return false;
            }
        }
    };

    private static final CharPredicate PREVIOUS_DEFINE_CHAR_CHECKER = new CharPredicate() {
        @Override
        public boolean test(final char value) {
            switch (value) {
                case ' ':
                case '\n':
                case '\r':
                case '(': {
                    return true;
                }
                default:
                    return false;
            }
        }
    };

    private static final CharPredicate NEXT_DEFINE_CHAR_CHECKER = new CharPredicate() {
        @Override
        public boolean test(final char value) {
            switch (value) {
                case ' ':
                case '\n':
                case '\r':
                case ')': {
                    return true;
                }
                default:
                    return false;
            }
        }
    };

    /**
     * Checks the name of define.
     *
     * @param defineName the define name.
     * @return true if this define is shader node define.
     */
    public static boolean isShaderNodeDefine(final String defineName) {
        return defineName.startsWith("SD_USE_") || defineName.startsWith("SD_IS_SET_");
    }

    /**
     * Checks the name of define.
     *
     * @param defineName the define name.
     * @return true if this define is shader node variable define.
     */
    public static boolean isShaderNodeVarDefine(final String defineName) {
        return defineName.startsWith("SD_IS_SET_");
    }

    /**
     * Converts the define name to the result define name.
     *
     * @param shaderNode the shader node.
     * @param defineName the define name.
     * @return the result define name.
     */
    public static String toResultDefineVarName(final ShaderNode shaderNode, final String defineName) {
        return shaderNode.getName() + "_SD_IS_SET_" + defineName;
    }

    /**
     * Find all existing nodes.
     *
     * @param node the node.
     * @param type the type.
     * @param <T>  the type.
     * @return the list of all found nodes.
     */
    public static <T extends ASTNode> List<T> findAllByType(final ASTNode node, final Class<T> type) {
        return findAllByType(node, new ArrayList<>(4), type);
    }

    /**
     * Find all existing nodes.
     *
     * @param node   the node.
     * @param result the result.
     * @param type   the type.
     * @param <T>    the type.
     * @return the list of all found nodes.
     */
    public static <T extends ASTNode> List<T> findAllByType(final ASTNode node, final List<T> result,
                                                            final Class<T> type) {

        node.visit(new Predicate<ASTNode>() {

            @Override
            public boolean test(final ASTNode node) {
                if (type.isInstance(node)) {
                    result.add(type.cast(node));
                }
                return true;
            }
        });

        return result;
    }

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

    /**
     * Updates offset, length and text of the node.
     *
     * @param node    the node.
     * @param content the content.
     */
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

    /**
     * Updates length and text of the node.
     *
     * @param node    the node.
     * @param content the content.
     */
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

    /**
     * Updates text of the node.
     *
     * @param node    the node.
     * @param content the content.
     */
    public static void updateText(final ASTNode node, final char[] content) {
        node.setText(String.valueOf(content, node.getOffset(), node.getLength()));
    }

    /**
     * Gets indent of the node.
     *
     * @param node the node.
     * @return the indent.
     */
    public static String getIndent(final ASTNode node) {

        int count = 0;

        ASTNode parent = node.getParent();
        while (parent != null) {
            count++;
            parent = parent.getParent();
        }

        return getIndent(count);
    }

    /**
     * Gets indent of the level.
     *
     * @param level the level.
     * @return the indent.
     */
    public static String getIndent(final int level) {

        final char[] result = new char[level * 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = ' ';
        }

        return String.valueOf(result);
    }

    /**
     * Removed duplicates of the extensions.
     *
     * @param extensions the extensions.
     */
    public static void removeExtensionDuplicates(final List<ExtensionPreprocessorASTNode> extensions) {

        if (extensions.size() < 2) {
            return;
        }

        for (Iterator<ExtensionPreprocessorASTNode> iterator = extensions.iterator(); iterator.hasNext(); ) {

            final ExtensionPreprocessorASTNode extension = iterator.next();
            final NameASTNode name = extension.getExtension();

            boolean isDuplicate = false;
            for (final ExtensionPreprocessorASTNode other : extensions) {
                if (other != extension && Objects.equals(name, other.getExtension())) {
                    isDuplicate = true;
                    break;
                }
            }

            if (isDuplicate) {
                iterator.remove();
            }
        }
    }

    /**
     * Removed duplicates of the define values.
     *
     * @param defineValues the define values.
     */
    public static void removeDefineValueDuplicates(final List<DefineValueASTNode> defineValues) {

        if (defineValues.size() < 2) {
            return;
        }

        for (Iterator<DefineValueASTNode> iterator = defineValues.iterator(); iterator.hasNext(); ) {

            final DefineValueASTNode defineValue = iterator.next();
            final String value = defineValue.getValue();

            boolean isDuplicate = false;
            for (final DefineValueASTNode other : defineValues) {
                if (other != defineValue && Objects.equals(value, other.getValue())) {
                    isDuplicate = true;
                    break;
                }
            }

            if (isDuplicate) {
                iterator.remove();
            }
        }
    }

    /**
     * Copies only global uniforms from the first list to the second.
     *
     * @param fields the list of external field.
     * @param result the result list.
     */
    public static void copyGlobalUniforms(final List<ExternalFieldDeclarationASTNode> fields,
                                          final List<ExternalFieldDeclarationASTNode> result) {
        if (fields.isEmpty()) {
            return;
        }

        for (final ExternalFieldDeclarationASTNode field : fields) {

            final ExternalFieldType type = field.getFieldType();
            if (type != ExternalFieldType.UNIFORM) {
                continue;
            }

            final NameASTNode nameNode = field.getName();
            final String name = nameNode.getName();

            if (!name.startsWith("g_")) {
                continue;
            }

            if (!result.contains(field)) {
                result.add(field);
            }
        }
    }

    /**
     * Removes exists global uniforms from the fields list.
     *
     * @param fields   the fields list.
     * @param bindings the list of exists global uniforms.
     */
    public static void removeExists(final List<ExternalFieldDeclarationASTNode> fields,
                                    final List<UniformBinding> bindings) {

        if (fields.isEmpty() || bindings.isEmpty()) {
            return;
        }

        for (Iterator<ExternalFieldDeclarationASTNode> iterator = fields.iterator(); iterator.hasNext(); ) {

            final ExternalFieldDeclarationASTNode field = iterator.next();

            final NameASTNode nameNode = field.getName();
            final String name = nameNode.getName();

            final UniformBinding binding = UniformBinding.valueOf(name.substring(2, name.length()));

            if (bindings.contains(binding)) {
                iterator.remove();
            }
        }
    }

    /**
     * Copies defined variables from the list of define value nodes.
     *
     * @param defineValueNodes the list of all define value nodes.
     * @param definedVariables the lit of defined variables.
     */
    public static void copyDefinedVariables(final List<DefineValueASTNode> defineValueNodes,
                                            final List<String> definedVariables) {

        if (defineValueNodes.isEmpty()) {
            return;
        }

        for (final DefineValueASTNode defineValueNode : defineValueNodes) {
            final String value = defineValueNode.getValue();
            if (!isShaderNodeVarDefine(value)) {
                continue;
            }

            definedVariables.add(value.substring("SD_IS_SET_".length(), value.length()));
        }
    }

    /**
     * Replaces a name of a variable in the source code.
     *
     * @param source  the source code.
     * @param oldName the old name.
     * @param newName the new name.
     * @return the updated string.
     */
    public static String replaceVar(final String source, final String oldName, final String newName) {
        return replace(source, oldName, newName, PREVIOUS_VAR_CHAR_CHECKER, NEXT_VAR_CHAR_CHECKER);
    }

    /**
     * Replaces a name of a define in the source code.
     *
     * @param source  the source code.
     * @param oldName the old name.
     * @param newName the new name.
     * @return the updated string.
     */
    public static String replaceDefine(final String source, final String oldName, final String newName) {
        return replace(source, oldName, newName, PREVIOUS_DEFINE_CHAR_CHECKER, NEXT_DEFINE_CHAR_CHECKER);
    }

    /**
     * Replaces a name of a method in the source code.
     *
     * @param source  the source code.
     * @param oldName the old name.
     * @param newName the new name.
     * @return the updated string.
     */
    public static String replaceMethod(final String source, final String oldName, final String newName) {
        return replace(source, oldName, newName, PREVIOUS_METHOD_CHAR_CHECKER, NEXT_METHOD_CHAR_CHECKER);
    }

    /**
     * Replaces a name in the source code.
     *
     * @param source          the source code.
     * @param oldName         the old name.
     * @param newName         the new name.
     * @param prevCharChecker the checker of a previous char.
     * @param nextCharChecker the checker of a next char.
     * @return the updated string.
     */
    public static String replace(final String source, final String oldName, final String newName,
                                 final CharPredicate prevCharChecker, final CharPredicate nextCharChecker) {

        if (!source.contains(oldName)) {
            return source;
        }

        final StringBuilder result = new StringBuilder(source.length() + newName.length());

        String debug = "";

        boolean copyOriginal = false;

        for (int i = 0, first = -1, current = 0, last = -1, length = source.length(); i < length; i++) {

            final char ch = source.charAt(i);

            if (first == -1) {

                if (oldName.charAt(0) != ch) {
                    result.append(ch);
                    continue;
                }

                first = i;
                current = 1;
                debug = String.valueOf(ch);
                continue;
            }

            debug += ch;

            if (current < oldName.length() && ch == oldName.charAt(current)) {
                current++;

                if (current >= oldName.length()) {
                    last = i;
                }

            } else if (current == oldName.length()) {
                i--;
                last = i;
            } else {
                last = i;
                copyOriginal = true;
            }

            if (last == -1) {
                continue;
            }

            if (copyOriginal) {
                result.append(source, first, last + 1);
                copyOriginal = false;
                first = -1;
                last = -1;
                continue;
            }

            char prevChar = ' ';
            char afterChar = ' ';

            if (first > 0) {
                prevChar = source.charAt(first - 1);
            }

            if (last < source.length() - 1) {
                afterChar = source.charAt(last + 1);
            }

            boolean canBeReplaced = prevCharChecker.test(prevChar);
            canBeReplaced = canBeReplaced && nextCharChecker.test(afterChar);

            if (canBeReplaced) {
                result.append(newName);
            } else {
                result.append(oldName);
            }

            first = -1;
            last = -1;
        }

        return result.toString();
    }
}
