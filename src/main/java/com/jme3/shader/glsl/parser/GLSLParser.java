package com.jme3.shader.glsl.parser;

import static java.util.Objects.requireNonNull;
import com.jme3.shader.glsl.parser.ast.*;
import com.jme3.shader.glsl.parser.ast.branching.condition.*;
import com.jme3.shader.glsl.parser.ast.declaration.FieldDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.FieldDeclarationASTNode.FieldType;
import com.jme3.shader.glsl.parser.ast.declaration.FileDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.MethodDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.ConditionalPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.PreprocessorBodyASTNode;
import com.jme3.shader.glsl.parser.ast.util.ASTUtils;

import java.util.*;

/**
 * The parser of GLSL code.
 *
 * @author JavaSaBr
 */
public class GLSLParser {

    private static final int TOKEN_DEFINE = 2;
    private static final int TOKEN_STRING = 3;
    private static final int TOKEN_KEYWORD = 4;
    private static final int TOKEN_WORD = 5;
    private static final int TOKEN_SEMICOLON = 6;
    private static final int TOKEN_LEFT_PARENTHESIS = 7;
    private static final int TOKEN_RIGHT_PARENTHESIS = 8;
    private static final int TOKEN_EXCLAMATION_MARK = 9;
    private static final int TOKEN_OR = 10;
    private static final int TOKEN_AND = 11;

    private static final Set<String> KEYWORDS = new HashSet<>();

    static {
        KEYWORDS.add("uniform");
        KEYWORDS.add("in");
        KEYWORDS.add("out");
        KEYWORDS.add("varying");
        KEYWORDS.add("attribute");
        KEYWORDS.add("discard");
        KEYWORDS.add("if");
        KEYWORDS.add("endif");
        KEYWORDS.add("defined");
        KEYWORDS.add("else");
        KEYWORDS.add("ifdef");
        KEYWORDS.add("ifndef");
        KEYWORDS.add("const");
        KEYWORDS.add("break");
        KEYWORDS.add("continue");
        KEYWORDS.add("do");
        KEYWORDS.add("for");
        KEYWORDS.add("while");
        KEYWORDS.add("inout");
        KEYWORDS.add("struct");
    }

    private static final Set<String> PREPROCESSOR = new HashSet<>();
    private static final Set<String> PREPROCESSOR_WITH_CONDITION = new HashSet<>();

    static {
        PREPROCESSOR_WITH_CONDITION.add("if");
        PREPROCESSOR_WITH_CONDITION.add("ifdef");
        PREPROCESSOR_WITH_CONDITION.add("ifndef");
        PREPROCESSOR_WITH_CONDITION.add("elif");

        PREPROCESSOR.addAll(PREPROCESSOR_WITH_CONDITION);
        PREPROCESSOR.add("define");
        PREPROCESSOR.add("undef");
        PREPROCESSOR.add("else");
        PREPROCESSOR.add("endif");
        PREPROCESSOR.add("error");
        PREPROCESSOR.add("pragma");
        PREPROCESSOR.add("extension");
        PREPROCESSOR.add("version");
        PREPROCESSOR.add("line");
    }

    private static final Set<Character> SPLIT_CHARS = new HashSet<>();

    static {
        SPLIT_CHARS.add(' ');
        SPLIT_CHARS.add('\n');
        SPLIT_CHARS.add('\t');
    }

    public static GLSLParser newInstance() {
        return new GLSLParser();
    }

    /**
     * The stack of ast nodes.
     */
    private final Deque<ASTNode> nodeStack;

    private Token currentToken;

    private int line;

    private int offset;

    private int savedLine;

    private int savedOffset;

    private GLSLParser() {
        this.nodeStack = new ArrayDeque<>();
    }

    /**
     * Parse the GLSL file.
     *
     * @param path     the path to GLSL file.
     * @param glslCode the GLSL code.
     * @return the file ast node.
     */
    public FileDeclarationASTNode parseFile(final String path, final String glslCode) {

        final FileDeclarationASTNode node = new FileDeclarationASTNode();
        node.setPath(path);
        node.setLine(line);
        node.setOffset(offset);
        node.setLength(glslCode.length());
        node.setText(glslCode);

        nodeStack.addLast(node);
        try {
            parseFileLevel(glslCode.toCharArray());
        } finally {
            nodeStack.removeLast();
        }

        return node;
    }

    private void parseFileLevel(final char[] content) {

        Token token;
        do {

            token = readToken(content);

            if (token.getType() == TOKEN_DEFINE) {
                parsePreprocessor(token, content);
            }
            // if it's a field declaration
            else if (token.getType() == TOKEN_KEYWORD && FieldType.forKeyWord(token.getText()) != null) {
                parseField(token, content);
            }

        } while (token != Token.EOF_TOKEN);
    }

    private void parseMethodLevel(final char[] content) {

        Token token;
        do {

            token = readToken(content);

            if (token.getType() == TOKEN_DEFINE) {
                parsePreprocessor(token, content);
            }

        } while (token != Token.EOF_TOKEN);
    }

    /**
     * Parse a define AST node.
     *
     * @param token   the define token.
     * @param content the content.
     */
    private void parsePreprocessor(final Token token, final char[] content) {

        final String text = token.getText();

        if (text.startsWith("#import")) {
            parseImport(token, content);
            return;
        }

        final String type = text.substring(1, text.length());

        if (!PREPROCESSOR_WITH_CONDITION.contains(type)) {
            return;
        }

        final ASTNode parent = nodeStack.getLast();
        final ConditionalPreprocessorASTNode node = new ConditionalPreprocessorASTNode();
        node.setParent(parent);
        node.setType(type);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());

        nodeStack.addLast(node);
        try {
            node.setCondition(parseCondition(type, content));
            node.setBody(parsePreprocessorBodyBranch(content));
        } finally {
            nodeStack.removeLast();
        }

        final ConditionASTNode conditionNode = node.getCondition();

        node.setLength(conditionNode.getOffset() + conditionNode.getLength() - token.getOffset());
        node.setText(new String(content, node.getOffset(), node.getLength()));

        parent.addChild(node);
    }

    /**
     * Parse a preprocessor body AST node.
     *
     * @param content the content.
     * @return the body AST node.
     */
    private PreprocessorBodyASTNode parsePreprocessorBodyBranch(final char[] content) {

        final ASTNode parent = nodeStack.getLast();
        final PreprocessorBodyASTNode node = new PreprocessorBodyASTNode();
        node.setParent(parent);

        nodeStack.addLast(node);
        try {

            final boolean isInsideMethod = ASTUtils.hasParentByType(node, MethodDeclarationASTNode.class);

            Token token;
            do {

                token = readToken(content);

                if (token.getType() == TOKEN_DEFINE) {

                    if (token.getText().equals("#endif")) {
                        offset = token.getOffset();
                        break;
                    }

                    parsePreprocessor(token, content);
                } else if (token.getType() == TOKEN_KEYWORD) {

                    if (FieldType.forKeyWord(token.getText()) != null) {

                        if (isInsideMethod) {
                            throw new IllegalArgumentException("unexpected field token");
                        }

                        parseField(token, content);
                    }
                }

            } while (token != Token.EOF_TOKEN);

        } finally {
            nodeStack.removeLast();
        }

        final List<ASTNode> children = node.getChildren();
        if (!children.isEmpty()) {

            final ASTNode first = children.get(0);
            final ASTNode last = children.get(children.size() - 1);

            node.setOffset(first.getOffset());
            node.setLength(last.getOffset() + last.getLength() - first.getOffset());
            node.setText(new String(content, node.getOffset(), node.getLength()));
        }

        parent.addChild(node);

        return node;
    }

    /**
     * Parse conditions.
     *
     * @param preprocessorType the type of a preprocessor or null.
     * @param content the content.
     * @return the condition AST node or null.
     */
    private ConditionASTNode parseCondition(final String preprocessorType, final char[] content) {

        if ("ifdef".equals(preprocessorType) || "ifndef".equals(preprocessorType)) {
            return parseIfDefPreprocessorCondition(findToken(content, TOKEN_WORD));
        }

        final Token token = findToken(content, TOKEN_KEYWORD, TOKEN_LEFT_PARENTHESIS, TOKEN_EXCLAMATION_MARK);

        switch (token.getType()) {
            case TOKEN_EXCLAMATION_MARK: {
                return parseNegativeCondition(content, token);
            }
            case TOKEN_KEYWORD: {

                final ConditionASTNode firstCondition = parseDefinePreprocessorCondition(content, token);

                if (isLastConditionPart(content)) {
                    return firstCondition;
                }

                final Token logicToken = findToken(content, TOKEN_OR, TOKEN_AND);

                final ASTNode parent = firstCondition.getParent();
                parent.removeChild(firstCondition);

                final MultiConditionASTNode conditionsNode =
                        logicToken.getType() == TOKEN_AND ? new ConditionAndASTNode() : new ConditionOrASTNode();
                conditionsNode.setParent(parent);
                conditionsNode.addChild(firstCondition);
                conditionsNode.addExpression(firstCondition);
                conditionsNode.setLine(firstCondition.getLine());
                conditionsNode.setOffset(firstCondition.getOffset());

                firstCondition.setParent(conditionsNode);

                nodeStack.addLast(conditionsNode);
                try {

                    conditionsNode.addExpression(parseSymbol(logicToken));

                    final ConditionASTNode secondCondition = requireNonNull(parseCondition(null, content));
                    conditionsNode.addExpression(secondCondition);

                    conditionsNode.setLength(secondCondition.getOffset() + secondCondition.getLength() - conditionsNode.getOffset());
                    conditionsNode.setText(new String(content, conditionsNode.getOffset(), conditionsNode.getLength()));

                } finally {
                    nodeStack.removeLast();
                }

                parent.addChild(conditionsNode);

                return conditionsNode;
            }
            case TOKEN_LEFT_PARENTHESIS: {

                final ASTNode parent = nodeStack.getLast();
                final ConditionIsASTNode node = new ConditionIsASTNode();
                node.setParent(parent);
                node.setLine(token.getLine());
                node.setOffset(token.getOffset());

                nodeStack.addLast(node);
                try {

                    parseSymbol(token);

                    node.setExpression(parseCondition(null, content));

                    final Token rightToken = findToken(content, TOKEN_RIGHT_PARENTHESIS);
                    final SymbolASTNode rightNode = parseSymbol(rightToken);

                    node.setLength(rightNode.getOffset() + rightNode.getLength() - node.getOffset());
                    node.setText(new String(content, node.getOffset(), node.getLength()));

                } finally {
                    nodeStack.removeLast();
                }

                parent.addChild(node);

                return node;
            }
        }

        return null;
    }

    /**
     * Checks of existing a next part of the current condition.
     *
     * @param content the content.
     * @return false if the next part is exists.
     */
    private boolean isLastConditionPart(final char[] content) {

        saveState();
        try {

            final Token check = findToken(content, TOKEN_KEYWORD, TOKEN_WORD, TOKEN_OR, TOKEN_AND,
                    TOKEN_RIGHT_PARENTHESIS);

            if (check.getType() == TOKEN_WORD || check.getType() == TOKEN_KEYWORD ||
                    check.getType() == TOKEN_RIGHT_PARENTHESIS) {
                throw new IllegalArgumentException();
            }

        } catch (final IllegalArgumentException e) {
            return true;
        } finally {
            restoreState();
        }

        return false;
    }

    /**
     * Parse a define preprocessor condition AST node.
     *
     * @param content the content.
     * @param token   the token.
     * @return the condition AST node.
     */
    private ConditionASTNode parseDefinePreprocessorCondition(final char[] content, final Token token) {

        final Token leftToken = findToken(content, TOKEN_LEFT_PARENTHESIS);
        final Token defineValueToken = findToken(content, TOKEN_WORD);
        final Token rightToken = findToken(content, TOKEN_RIGHT_PARENTHESIS);

        final ASTNode parent = nodeStack.getLast();
        final ConditionIsASTNode node = new ConditionIsASTNode();
        node.setParent(parent);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());

        nodeStack.addLast(node);
        try {
            parseSymbol(leftToken);
            node.setExpression(parseDefineValue(defineValueToken));
            parseSymbol(rightToken);
        } finally {
            nodeStack.removeLast();
        }

        node.setLength(rightToken.getOffset() + rightToken.getLength() - token.getOffset());
        node.setText(new String(content, node.getOffset(), node.getLength()));

        parent.addChild(node);

        return node;
    }

    /**
     * Parse a condition not ast node.
     *
     * @param content the content.
     * @param token   the condition token.
     * @return the result condition node.
     */
    private ConditionASTNode parseNegativeCondition(final char[] content, final Token token) {

        final ASTNode parent = nodeStack.getLast();
        final ConditionIsNotASTNode node = new ConditionIsNotASTNode();
        node.setParent(parent);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());

        nodeStack.addLast(node);
        try {
            parseSymbol(token);
            node.setExpression(parseCondition(null, content));
        } finally {
            nodeStack.removeLast();
        }

        final ASTNode expression = node.getExpression();

        node.setLength(expression.getOffset() + expression.getLength() - token.getOffset());
        node.setText(new String(content, node.getOffset(), node.getLength()));

        parent.addChild(node);

        return node;
    }

    /**
     * Parse the condition of ifdef/idndef constructions.
     *
     * @param token the condition token.
     * @return the condition.
     */
    private ConditionASTNode parseIfDefPreprocessorCondition(final Token token) {

        final ASTNode parent = nodeStack.getLast();
        final ConditionIsASTNode node = new ConditionIsASTNode();
        node.setParent(parent);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());
        node.setLength(token.getOffset() + token.getLength() - token.getOffset());
        node.setText(token.getText());

        nodeStack.addLast(node);
        try {
            node.setExpression(parseDefineValue(token));
        } finally {
            nodeStack.removeLast();
        }

        parent.addChild(node);

        return node;
    }

    /**
     * Parse an import AST node.
     *
     * @param defineToken the define token.
     * @param content     the content.
     */
    private void parseImport(final Token defineToken, final char[] content) {

        final Token token = findToken(content, TOKEN_STRING);

        final ASTNode parent = nodeStack.getLast();
        final ImportASTNode node = new ImportASTNode();
        node.setParent(parent);
        node.setLine(defineToken.getLine());
        node.setOffset(defineToken.getOffset());
        node.setLength(token.getOffset() + token.getLength() - defineToken.getOffset());
        node.setText(new String(content, node.getOffset(), node.getLength()));

        parent.getChildren().add(node);

        nodeStack.addLast(node);
        try {
            parseStringValue(token);
        } finally {
            nodeStack.removeLast();
        }
    }

    /**
     * Parse a field AST node.
     *
     * @param keyWordToken the key word token.
     * @param content      the content.
     */
    private void parseField(final Token keyWordToken, final char[] content) {

        final Token typeToken = findToken(content, TOKEN_WORD);
        final Token nameToken = findToken(content, TOKEN_WORD);
        final Token semicolonToken = findToken(content, TOKEN_SEMICOLON);

        final ASTNode parent = nodeStack.getLast();
        final FieldDeclarationASTNode node = new FieldDeclarationASTNode();
        node.setParent(parent);
        node.setLine(keyWordToken.getLine());
        node.setOffset(keyWordToken.getOffset());
        node.setLength(semicolonToken.getOffset() + semicolonToken.getLength() - keyWordToken.getOffset());
        node.setText(new String(content, node.getOffset(), node.getLength()));
        node.setFieldType(FieldType.forKeyWord(keyWordToken.getText()));

        nodeStack.addLast(node);
        try {
            node.setType(parseType(typeToken));
            node.setName(parseName(nameToken));
            parseSymbol(semicolonToken);
        } finally {
            nodeStack.removeLast();
        }

        parent.addChild(node);
    }

    /**
     * Parse a define value AST node.
     *
     * @param defineValueToken the define value token.
     */
    private DefineValueASTNode parseDefineValue(final Token defineValueToken) {

        final String text = defineValueToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final DefineValueASTNode node = new DefineValueASTNode();
        node.setParent(parent);
        node.setLine(defineValueToken.getLine());
        node.setOffset(defineValueToken.getOffset());
        node.setLength(defineValueToken.getLength());
        node.setText(text);
        node.setName(text);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse a type AST node.
     *
     * @param typeToken the type token.
     */
    private TypeASTNode parseType(final Token typeToken) {

        final String text = typeToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final TypeASTNode node = new TypeASTNode();
        node.setParent(parent);
        node.setLine(typeToken.getLine());
        node.setOffset(typeToken.getOffset());
        node.setLength(typeToken.getLength());
        node.setText(text);
        node.setName(text);

        parent.getChildren().add(node);

        return node;
    }

    /**
     * Parse a name AST node.
     *
     * @param nameToken the name token.
     */
    private NameASTNode parseName(final Token nameToken) {

        final String text = nameToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final NameASTNode node = new NameASTNode();
        node.setParent(parent);
        node.setLine(nameToken.getLine());
        node.setOffset(nameToken.getOffset());
        node.setLength(nameToken.getLength());
        node.setText(text);
        node.setName(text);

        parent.getChildren().add(node);

        return node;
    }

    /**
     * Parse a string AST node.
     *
     * @param stringToken the string token.
     */
    private void parseStringValue(final Token stringToken) {

        final String text = stringToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final StringASTNode node = new StringASTNode();
        node.setParent(parent);
        node.setLine(stringToken.getLine());
        node.setOffset(stringToken.getOffset());
        node.setLength(stringToken.getLength());
        node.setText(text);

        if (text.equals("\"\"")) {
            node.setValue("");
        } else {
            node.setValue(text.substring(1, text.length() - 1));
        }

        parent.getChildren().add(node);
    }

    /**
     * Parse a symbol AST node.
     *
     * @param symbolToken the symbol token.
     * @return the node.
     */
    private SymbolASTNode parseSymbol(final Token symbolToken) {

        final String text = symbolToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final SymbolASTNode node = new SymbolASTNode();
        node.setParent(parent);
        node.setLine(symbolToken.getLine());
        node.setOffset(symbolToken.getOffset());
        node.setLength(symbolToken.getLength());
        node.setText(text);

        parent.addChild(node);

        return node;
    }

    private Token getCurrentToken() {
        return currentToken;
    }

    private void setCurrentToken(final Token currentToken) {
        this.currentToken = currentToken;
    }

    private Token findToken(final char[] content, final int type) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != type && token.getType() != Token.EOF);

        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType && token.getType() != Token.EOF);

        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType, final int thirdType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType && token.getType() != thirdType &&
                token.getType() != Token.EOF);

        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType, final int thirdType,
                            final int fourthType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType && token.getType() != thirdType &&
                token.getType() != fourthType && token.getType() != Token.EOF);

        if (token.getType() == Token.EOF) {
            throw new IllegalArgumentException("unexpected EOF token");
        }

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType, final int thirdType,
                            final int fourthType, final int fifthType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType && token.getType() != thirdType &&
                token.getType() != fourthType && token.getType() != fifthType && token.getType() != Token.EOF);

        if (token.getType() == Token.EOF) {
            throw new IllegalArgumentException("unexpected EOF token");
        }

        return token;
    }

    private Token readToken(final char[] content) {

        String text = null;

        while (true) {

            if (offset >= content.length) {
                return Token.EOF_TOKEN;
            }

            final char ch = content[offset++];

            if (SPLIT_CHARS.contains(ch)) {

                final Token token = getCurrentToken();

                if (token != null) {
                    offset--;
                    setCurrentToken(null);
                    token.setText(getText(text));
                    token.setLength(offset - token.getOffset());
                    return token;
                } else if (isWord(text)) {
                    offset--;
                    return new Token(TOKEN_WORD, offset - text.length(), line, text);
                }

                if (ch == '\n') {
                    line++;
                }

                return Token.SKIP_TOKEN;
            }

            switch (ch) {
                case ';': {
                    return handleCharToken(text, ch, TOKEN_SEMICOLON);
                }
                case '(': {
                    return handleCharToken(text, ch, TOKEN_LEFT_PARENTHESIS);
                }
                case ')': {
                    return handleCharToken(text, ch, TOKEN_RIGHT_PARENTHESIS);
                }
                case '!': {
                    return handleCharToken(text, ch, TOKEN_EXCLAMATION_MARK);
                }
            }

            if (text == null) {
                text = Character.toString(ch);
            } else {
                text += ch;
            }

            switch (ch) {
                case '"': {

                    final Token currentToken = getCurrentToken();
                    if (currentToken != null) {
                        currentToken.setText(getText(text));
                        currentToken.setLength(offset - currentToken.getOffset());
                        setCurrentToken(null);
                        return currentToken;
                    }

                    setCurrentToken(new Token(TOKEN_STRING, line, offset - 1));
                    continue;
                }
                case '#': {
                    setCurrentToken(new Token(TOKEN_DEFINE, line, offset - 1));
                    continue;
                }
            }

            if (currentToken != null) {
                continue;
            }

            if (KEYWORDS.contains(text)) {
                return new Token(TOKEN_KEYWORD, offset - text.length(), line, text);
            } else if ("||".equals(text)) {
                return new Token(TOKEN_OR, offset - text.length(), line, text);
            } else if ("&&".equals(text)) {
                return new Token(TOKEN_AND, offset - text.length(), line, text);
            }
        }
    }

    private Token handleCharToken(final String text, final char ch, final int charTokenType) {

        if (isWord(text)) {
            offset--;
            return new Token(TOKEN_WORD, offset - text.length(), line, text);
        }

        return new Token(charTokenType, offset - 1, line, Character.toString(ch));
    }

    private boolean isWord(final String text) {

        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0, length = text.length(); i < length; i++) {
            final char ch = text.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_') {
                return false;
            }
        }

        return true;
    }

    private String getText(final String text) {
        return text == null ? "" : text;
    }

    private void saveState() {
        savedLine = line;
        savedOffset = offset;
    }

    private void restoreState() {
        line = savedLine;
        offset = savedOffset;
    }
}
