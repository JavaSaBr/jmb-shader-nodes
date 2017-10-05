package com.jme3.shader.glsl.parser;

import static com.jme3.shader.glsl.parser.GLSLLang.KEYWORDS;
import static com.jme3.shader.glsl.parser.GLSLLang.PREPROCESSOR_WITH_CONDITION;
import static java.util.Objects.requireNonNull;
import com.jme3.shader.glsl.parser.ast.*;
import com.jme3.shader.glsl.parser.ast.branching.condition.*;
import com.jme3.shader.glsl.parser.ast.declaration.*;
import com.jme3.shader.glsl.parser.ast.declaration.ExternalFieldDeclarationASTNode.ExternalFieldType;
import com.jme3.shader.glsl.parser.ast.preprocessor.ConditionalPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.PreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.util.ASTUtils;
import com.jme3.shader.glsl.parser.ast.util.Predicate;
import com.jme3.shader.glsl.parser.ast.value.DefineValueASTNode;
import com.jme3.shader.glsl.parser.ast.value.StringValueASTNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * The parser of GLSL code.
 *
 * @author JavaSaBr
 */
public class GLSLParser {

    public static final int TOKEN_DEFINE = 2;
    public static final int TOKEN_STRING = 3;
    public static final int TOKEN_KEYWORD = 4;
    public static final int TOKEN_WORD = 5;
    public static final int TOKEN_SEMICOLON = 6;
    public static final int TOKEN_LEFT_PARENTHESIS = 7;
    public static final int TOKEN_RIGHT_PARENTHESIS = 8;
    public static final int TOKEN_EXCLAMATION_MARK = 9;
    public static final int TOKEN_OR = 10;
    public static final int TOKEN_AND = 11;
    public static final int TOKEN_LEFT_BRACE = 12;
    public static final int TOKEN_RIGHT_BRACE = 13;
    public static final int TOKEN_ASSIGN = 14;

    public static final int LEVEL_FILE = 1;
    public static final int LEVEL_STRUCT = 2;
    public static final int LEVEL_METHOD = 3;

    private static final Set<Character> SPLIT_CHARS = new HashSet<>();

    static {
        SPLIT_CHARS.add(' ');
        SPLIT_CHARS.add('\n');
        SPLIT_CHARS.add('\t');
    }

    /**
     * Creates a new instance of this parser.
     *
     * @return the new instance of this parser.
     */
    public static GLSLParser newInstance() {
        return new GLSLParser();
    }

    /**
     * The stack of ast nodes.
     */
    private final Deque<ASTNode> nodeStack;

    /**
     * The current token.
     */
    private Token currentToken;

    /**
     * The current line.
     */
    private int line;

    /**
     * The current offset.
     */
    private int offset;

    /**
     * The saved line.
     */
    private int savedLine;

    /**
     * The saved offset.
     */
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
    public FileDeclarationASTNode parseFileDeclaration(final String path, final String glslCode) {

        final FileDeclarationASTNode node = new FileDeclarationASTNode();
        node.setPath(path);
        node.setLine(line);
        node.setOffset(offset);
        node.setLength(glslCode.length());
        node.setText(glslCode);

        nodeStack.addLast(node);
        try {
            parseContent(glslCode.toCharArray(), LEVEL_FILE, ASTUtils.EMPTY);
        } finally {
            nodeStack.removeLast();
        }

        System.out.println(node.toString());

        return node;
    }

    /**
     * Parse content.
     *
     * @param content       the content.
     * @param level         the level.
     * @param exitCondition the exit condition.
     */
    private void parseContent(final char[] content, final int level, final Predicate<Token> exitCondition) {

        int prevOffset;
        int prevLine;

        Token token;
        do {

            prevOffset = offset;
            prevLine = line;
            token = readToken(content);

            if (exitCondition.test(token)) {
                offset = prevOffset;
                line = prevLine;
                return;
            }

            if (token.getType() == TOKEN_DEFINE) {
                parsePreprocessor(token, content);
            } else if (token.getType() == TOKEN_KEYWORD) {

                if (level == LEVEL_FILE) {
                    parseExternalFieldDeclaration(token, content);
                }

            } else if (token.getType() == TOKEN_WORD) {

                if (level == LEVEL_FILE) {
                    parseMethodDeclaration(token, content);
                } else if(level == LEVEL_METHOD) {

                    Token secondToken;

                    saveState();
                    try {
                        secondToken = findToken(content, TOKEN_ASSIGN, TOKEN_WORD);
                    } finally {
                        restoreState();
                    }

                    if (secondToken.getType() == TOKEN_WORD) {
                        parseLocalVarDeclaration(token, content);
                    } else if (secondToken.getType() == TOKEN_ASSIGN) {
                        parseAssignExpression(token, content);
                    }
                }
            }

        } while (token != Token.EOF_TOKEN);
    }

    /**
     * Parse a preprocessor AST node.
     *
     * @param token   the preprocessor token.
     * @param content the content.
     * @return the preprocessor AST node.
     */
    private PreprocessorASTNode parsePreprocessor(final Token token, final char[] content) {

        final String text = token.getText();

        if (text.startsWith("#import")) {
            parseImportDeclaration(content, token);
            return null;
        }

        final String type = text.substring(1, text.length());

        if (!PREPROCESSOR_WITH_CONDITION.contains(type)) {
            return parsePreprocessor(token, type);
        }

        return parseConditionalPreprocessor(token, content, type);
    }

    /**
     * Parse a preprocessor AST node.
     *
     * @param token the preprocessor token.
     * @param type  the type of the preprocessor.
     * @return the preprocessor AST node.
     */
    private PreprocessorASTNode parsePreprocessor(final Token token, final String type) {

        final ASTNode parent = nodeStack.getLast();
        final PreprocessorASTNode node = new PreprocessorASTNode();
        node.setParent(parent);
        node.setType(type);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());
        node.setLength(token.getLength());
        node.setText(token.getText());

        parent.addChild(node);

        return node;
    }

    /**
     * Parse a conditional preprocessor AST node.
     *
     * @param token   the conditional preprocessor token.
     * @param content the content.
     * @param type    the type of the preprocessor.
     * @return the conditional preprocessor AST node.
     */
    private ConditionalPreprocessorASTNode parseConditionalPreprocessor(final Token token, final char[] content,
                                                                        final String type) {

        final ASTNode parent = nodeStack.getLast();
        final ConditionalPreprocessorASTNode node = new ConditionalPreprocessorASTNode();
        node.setParent(parent);
        node.setType(type);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());

        nodeStack.addLast(node);
        try {

            node.setCondition(parseCondition(type, content));
            node.setBody(parseBody(content, ASTUtils.END_IF_OR_ELSE_OR_ELSE_IF));

            final Token nextDefine = findToken(content, TOKEN_DEFINE);
            final String nextTokenText = nextDefine.getText();

            if (GLSLLang.PR_ENDIF.equals(nextTokenText)) {
                node.setEndNode(parsePreprocessor(nextDefine, content));
            } else if (GLSLLang.PR_ELSE.equals(nextTokenText)) {
                node.setElseNode(parsePreprocessor(nextDefine, content));
                node.setElseBody(parseBody(content, ASTUtils.END_IF));
                node.setEndNode(parsePreprocessor(findToken(content, TOKEN_DEFINE), content));
            } else if (GLSLLang.PR_ELIF.equals(nextTokenText)) {

                final BodyASTNode body = new BodyASTNode();
                body.setParent(node);

                nodeStack.addLast(body);
                try {
                    parsePreprocessor(nextDefine, content);
                } finally {
                    nodeStack.removeLast();
                }

                ASTUtils.updateOffsetAndLengthAndText(body, content);

                node.addChild(body);
                node.setElseBody(body);

                final ConditionalPreprocessorASTNode lastNode = node.getLastNode(ConditionalPreprocessorASTNode.class);
                final PreprocessorASTNode endNode = lastNode.getEndNode();
                final Token endToken = new Token(TOKEN_DEFINE, endNode.getOffset(), endNode.getLine(), endNode.getText());

                node.setEndNode(parsePreprocessor(endToken, content));
            }

        } finally {
            nodeStack.removeLast();
        }

        ASTUtils.updateLengthAndText(node, content);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse a body AST node.
     *
     * @param content       the content.
     * @param exitCondition the exit condition.
     * @return the body AST node.
     */
    private BodyASTNode parseBody(final char[] content, final Predicate<Token> exitCondition) {

        final ASTNode parent = nodeStack.getLast();
        final BodyASTNode node = new BodyASTNode();
        node.setParent(parent);

        nodeStack.addLast(node);
        try {
            parseContent(content, ASTUtils.getParseLevel(node), exitCondition);
        } finally {
            nodeStack.removeLast();
        }

        ASTUtils.updateOffsetAndLengthAndText(node, content);

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

        if (GLSLLang.PR_TYPE_IFDEF.equals(preprocessorType) || GLSLLang.PR_TYPE_IFNDEF.equals(preprocessorType)) {
            return parseSimpleDefPreprocessorCondition(content);
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

                } finally {
                    nodeStack.removeLast();
                }

                ASTUtils.updateLengthAndText(conditionsNode, content);

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
                    parseSymbol(findToken(content, TOKEN_RIGHT_PARENTHESIS));
                } finally {
                    nodeStack.removeLast();
                }

                ASTUtils.updateLengthAndText(node, content);

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

        ASTUtils.updateLengthAndText(node, content);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse a condition is not AST node.
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

        ASTUtils.updateLengthAndText(node, content);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse the condition of ifdef/idndef constructions.
     *
     * @param content the content.
     * @return the condition.
     */
    private ConditionASTNode parseSimpleDefPreprocessorCondition(final char[] content) {

        final Token token = findToken(content, TOKEN_WORD);
        final ASTNode parent = nodeStack.getLast();
        final ConditionIsASTNode node = new ConditionIsASTNode();
        node.setParent(parent);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());

        nodeStack.addLast(node);
        try {
            node.setExpression(parseDefineValue(token));
        } finally {
            nodeStack.removeLast();
        }

        parent.addChild(node);

        ASTUtils.updateLengthAndText(node, content);

        return node;
    }

    /**
     * Parse an import declaration AST node.
     *
     * @param content     the content.
     * @param defineToken the define token.
     * @return the import declaration AST node.
     */
    private ImportDeclarationASTNode parseImportDeclaration(final char[] content, final Token defineToken) {

        final Token token = findToken(content, TOKEN_STRING);

        final ASTNode parent = nodeStack.getLast();
        final ImportDeclarationASTNode node = new ImportDeclarationASTNode();
        node.setParent(parent);
        node.setLine(defineToken.getLine());
        node.setOffset(defineToken.getOffset());

        nodeStack.addLast(node);
        try {
            parseStringValue(token);
        } finally {
            nodeStack.removeLast();
        }

        ASTUtils.updateLengthAndText(node, content);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse an external field declaration AST node.
     *
     * @param fieldTypeToken the field type token.
     * @param content        the content.
     * @return the external field declaration AST node.
     */
    private ExternalFieldDeclarationASTNode parseExternalFieldDeclaration(final Token fieldTypeToken,
                                                                          final char[] content) {

        final Token typeToken = findToken(content, TOKEN_WORD);
        final Token nameToken = findToken(content, TOKEN_WORD);
        final Token semicolonToken = findToken(content, TOKEN_SEMICOLON);

        final ASTNode parent = nodeStack.getLast();
        final ExternalFieldDeclarationASTNode node = new ExternalFieldDeclarationASTNode();
        node.setParent(parent);
        node.setLine(fieldTypeToken.getLine());
        node.setOffset(fieldTypeToken.getOffset());
        node.setFieldType(ExternalFieldType.forKeyWord(fieldTypeToken.getText()));

        nodeStack.addLast(node);
        try {
            node.setType(parseType(typeToken));
            node.setName(parseName(nameToken));
            parseSymbol(semicolonToken);
        } finally {
            nodeStack.removeLast();
        }

        ASTUtils.updateLengthAndText(node, content);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse a method declaration AST node.
     *
     * @param returnTypeToken the type
     * @param content         the content.
     */
    private void parseMethodDeclaration(final Token returnTypeToken, final char[] content) {

        final Token nameToken = findToken(content, TOKEN_WORD);
        final Token startArgumentsToken = findToken(content, TOKEN_LEFT_PARENTHESIS);
        final Token finishArgumentsToken = findToken(content, TOKEN_RIGHT_PARENTHESIS);
        final Token startBodyToken = findToken(content, TOKEN_LEFT_BRACE);

        final ASTNode parent = nodeStack.getLast();
        final MethodDeclarationASTNode node = new MethodDeclarationASTNode();
        node.setParent(parent);
        node.setLine(returnTypeToken.getLine());
        node.setOffset(returnTypeToken.getOffset());

        nodeStack.addLast(node);
        try {

            node.setReturnType(parseType(returnTypeToken));
            node.setName(parseName(nameToken));

            parseSymbol(startArgumentsToken);
            parseSymbol(finishArgumentsToken);
            parseSymbol(startBodyToken);

            node.setBody(parseBody(content, ASTUtils.RIGHT_BRACE));

            final Token endBodyToken = findToken(content, TOKEN_RIGHT_BRACE);

            parseSymbol(endBodyToken);

            node.setLength(endBodyToken.getOffset() + endBodyToken.getLength() - returnTypeToken.getOffset());
            node.setText(new String(content, node.getOffset(), node.getLength()));

        } finally {
            nodeStack.removeLast();
        }

        parent.addChild(node);
    }

    /**
     * Parse a local variable declaration AST node.
     *
     * @param typeToken the var type token.
     * @param content   the content.
     * @return the local variable declaration AST node.
     */
    private LocalVarDeclarationASTNode parseLocalVarDeclaration(final Token typeToken, final char[] content) {

        final Token nameToken = findToken(content, TOKEN_WORD);
        final Token semicolonOrAssignToken = findToken(content, TOKEN_SEMICOLON, TOKEN_ASSIGN);

        final ASTNode parent = nodeStack.getLast();
        final LocalVarDeclarationASTNode node = new LocalVarDeclarationASTNode();
        node.setParent(parent);
        node.setLine(typeToken.getLine());
        node.setOffset(typeToken.getOffset());

        nodeStack.addLast(node);
        try {

            node.setType(parseType(typeToken));
            node.setName(parseName(nameToken));

            if (semicolonOrAssignToken.getType() == TOKEN_SEMICOLON) {
                parseSymbol(semicolonOrAssignToken);
            } else {
                node.setAssign(null);
                parseSymbol(findToken(content, TOKEN_SEMICOLON));
            }

        } finally {
            nodeStack.removeLast();
        }

        ASTUtils.updateLengthAndText(node, content);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse an assign expression AST node.
     *
     * @param firstToken the first token.
     * @param content    the content.
     * @return the assign expression AST node.
     */
    private AssignExpressionASTNode parseAssignExpression(final Token firstToken, final char[] content) {

        final Token assignToken = findToken(content, TOKEN_ASSIGN);
        final Token endToken = findToken(content, TOKEN_SEMICOLON);

        final ASTNode parent = nodeStack.getLast();
        final AssignExpressionASTNode node = new AssignExpressionASTNode();
        node.setParent(parent);
        node.setLine(firstToken.getLine());
        node.setOffset(firstToken.getOffset());

        nodeStack.addLast(node);
        try {
            parseName(firstToken);
            parseSymbol(assignToken);
            parseSymbol(endToken);
        } finally {
            nodeStack.removeLast();
        }

        ASTUtils.updateLengthAndText(node, content);

        parent.addChild(node);

        return node;
    }

    /**
     * Parse a define value AST node.
     *
     * @param token the define value token.
     */
    private DefineValueASTNode parseDefineValue(final Token token) {

        final String text = token.getText();
        final ASTNode parent = nodeStack.getLast();
        final DefineValueASTNode node = new DefineValueASTNode();
        node.setParent(parent);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());
        node.setLength(token.getLength());
        node.setText(text);
        node.setValue(text);

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

        parent.addChild(node);

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

        parent.addChild(node);

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
        final StringValueASTNode node = new StringValueASTNode();
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

        parent.addChild(node);
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

    /**
     * Gets the current token.
     *
     * @return the current token.
     */
    private Token getCurrentToken() {
        return currentToken;
    }

    /**
     * Sets the current token.
     *
     * @param currentToken the current token.
     */
    private void setCurrentToken(final Token currentToken) {
        this.currentToken = currentToken;
    }

    /**
     * Finds a token by the type.
     *
     * @param content the content.
     * @param type    the type.
     * @return the found token.
     * @throws RuntimeException when we didn't find a token with the type.
     */
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

    /**
     * Finds a token by the types.
     *
     * @param content    the content.
     * @param firstType  the first type.
     * @param secondType the second type.
     * @return the found token.
     * @throws RuntimeException when we didn't find a token with the types.
     */
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

    /**
     * Finds a token by the types.
     *
     * @param content    the content.
     * @param firstType  the first type.
     * @param secondType the second type.
     * @param thirdType  the third type.
     * @return the found token.
     * @throws RuntimeException when we didn't find a token with the types.
     */
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

    /**
     * Finds a token by the types.
     *
     * @param content    the content.
     * @param firstType  the first type.
     * @param secondType the second type.
     * @param thirdType  the third type.
     * @param fourthType the fourth type.
     * @return the found token.
     * @throws RuntimeException when we didn't find a token with the types.
     */
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

    /**
     * Finds a token by the types.
     *
     * @param content    the content.
     * @param firstType  the first type.
     * @param secondType the second type.
     * @param thirdType  the third type.
     * @param fourthType the fourth type.
     * @param fifthType  the fifth type.
     * @return the found token.
     * @throws RuntimeException when we didn't find a token with the types.
     */
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

    /**
     * Reads a next token.
     *
     * @param content the content.
     * @return the next token.
     */
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
                case '{': {
                    return handleCharToken(text, ch, TOKEN_LEFT_BRACE);
                }
                case '}': {
                    return handleCharToken(text, ch, TOKEN_RIGHT_BRACE);
                }
                case '!': {
                    return handleCharToken(text, ch, TOKEN_EXCLAMATION_MARK);
                }
                case '=': {
                    return handleCharToken(text, ch, TOKEN_ASSIGN);
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

    /**
     * Build a char token.
     *
     * @param text          the text of this token.
     * @param ch            the symbol of this token.
     * @param charTokenType the type of this token.
     * @return the new token.
     */
    private Token handleCharToken(final String text, final char ch, final int charTokenType) {

        if (isWord(text)) {
            offset--;
            return new Token(TOKEN_WORD, offset - text.length(), line, text);
        }

        return new Token(charTokenType, offset - 1, line, Character.toString(ch));
    }

    /**
     * Checks the text.
     *
     * @param text the text.
     * @return true if the text is number or is word.
     */
    private boolean isWord(final String text) {

        if (text == null || text.isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(text);
            return true;
        } catch (final NumberFormatException e) {
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

    /**
     * Save the current state of the parser.
     */
    private void saveState() {
        savedLine = line;
        savedOffset = offset;
    }

    /**
     * Restore the saved state of this parser.
     */
    private void restoreState() {
        line = savedLine;
        offset = savedOffset;
    }
}
