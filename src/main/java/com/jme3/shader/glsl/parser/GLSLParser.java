package com.jme3.shader.glsl.parser;

import com.jme3.shader.glsl.parser.ast.*;
import com.jme3.shader.glsl.parser.ast.FieldASTNode.FieldType;

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
    public FileASTNode parseFile(final String path, final String glslCode) {

        final FileASTNode node = new FileASTNode();
        node.setPath(path);
        node.setLine(line);
        node.setOffset(offset);
        node.setLength(glslCode.length());
        node.setText(glslCode);

        nodeStack.addLast(node);
        try {
            parseFile(glslCode.toCharArray());
        } finally {
            nodeStack.removeLast();
        }

        return node;
    }

    private void parseFile(final char[] content) {

        Token token;
        do {

            token = readToken(content);

            if (token.getType() == TOKEN_DEFINE) {
                parsePreprocessor(token, content);
            }
            // if it's field declaration
            else if (token.getType() == TOKEN_KEYWORD && FieldType.forKeyWord(token.getText()) != null) {
                parseField(token, content);
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
            node.setConditionNode(parsePreprocessorCondition(type, content));
        } finally {
            nodeStack.removeLast();
        }

        final ConditionASTNode conditionNode = node.getConditionNode();

        node.setLength(conditionNode.getOffset() + conditionNode.getLength() - token.getOffset());
        node.setText(new String(content, node.getOffset(), node.getLength()));

        parent.addChild(node);
    }

    private ConditionASTNode parsePreprocessorCondition(final String type, final char[] content) {

        if ("ifdef".equals(type) || "ifndef".equals(type)) {
            return parseIfDefCondition(findToken(content, TOKEN_WORD));
        }

        final Token token = findToken(content, TOKEN_KEYWORD, TOKEN_LEFT_PARENTHESIS, TOKEN_EXCLAMATION_MARK);

        switch (token.getType()) {
            case TOKEN_EXCLAMATION_MARK: {

                final ASTNode parent = nodeStack.getLast();
                final ConditionNotASTNode node = new ConditionNotASTNode();
                node.setParent(parent);
                node.setLine(token.getLine());
                node.setOffset(token.getOffset());

                nodeStack.addLast(node);
                try {
                    parseSymbol(token);
                    node.setValue(parsePreprocessorCondition(type, content));
                } finally {
                    nodeStack.removeLast();
                }

                final ASTNode value = node.getValue();

                node.setLength(value.getOffset() + value.getLength() - token.getOffset());
                node.setText(new String(content, node.getOffset(), node.getLength()));

                parent.addChild(node);

                return node;
            }
            case TOKEN_KEYWORD: {

                final Token leftToken = findToken(content, TOKEN_LEFT_PARENTHESIS);
                final Token defineValueToken = findToken(content, TOKEN_WORD);
                final Token rightToken = findToken(content, TOKEN_RIGHT_PARENTHESIS);

                final ASTNode parent = nodeStack.getLast();
                final ConditionASTNode conditionASTNode = new ConditionASTNode();
                conditionASTNode.setParent(parent);
                conditionASTNode.setLine(token.getLine());
                conditionASTNode.setOffset(token.getOffset());
                conditionASTNode.setLength(token.getOffset() + token.getLength() - token.getOffset());
                conditionASTNode.setText(token.getText());

                nodeStack.addLast(conditionASTNode);
                try {
                    parseSymbol(leftToken);
                    conditionASTNode.setValue(parseDefineValue(defineValueToken));
                    parseSymbol(rightToken);
                } finally {
                    nodeStack.removeLast();
                }

                parent.addChild(conditionASTNode);

                return conditionASTNode;
            }
            case TOKEN_LEFT_PARENTHESIS: {

            }
        }

        return null;
    }

    /**
     * Parse the condition of ifdef/idndef constructions.
     *
     * @param token the condition token.
     * @return the condition.
     */
    private ConditionASTNode parseIfDefCondition(final Token token) {

        final ASTNode parent = nodeStack.getLast();
        final ConditionASTNode node = new ConditionASTNode();
        node.setParent(parent);
        node.setLine(token.getLine());
        node.setOffset(token.getOffset());
        node.setLength(token.getOffset() + token.getLength() - token.getOffset());
        node.setText(token.getText());

        nodeStack.addLast(node);
        try {
            node.setValue(parseDefineValue(token));
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
        final ImportASTNode importASTNode = new ImportASTNode();
        importASTNode.setParent(parent);
        importASTNode.setLine(defineToken.getLine());
        importASTNode.setOffset(defineToken.getOffset());
        importASTNode.setLength(token.getOffset() + token.getLength() - defineToken.getOffset());
        importASTNode.setText(new String(content, importASTNode.getOffset(), importASTNode.getLength()));

        parent.getChildren().add(importASTNode);

        nodeStack.addLast(importASTNode);
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
        final FieldASTNode fieldASTNode = new FieldASTNode();
        fieldASTNode.setParent(parent);
        fieldASTNode.setLine(keyWordToken.getLine());
        fieldASTNode.setOffset(keyWordToken.getOffset());
        fieldASTNode.setLength(semicolonToken.getOffset() + semicolonToken.getLength() - keyWordToken.getOffset());
        fieldASTNode.setText(new String(content, fieldASTNode.getOffset(), fieldASTNode.getLength()));
        fieldASTNode.setFieldType(FieldType.forKeyWord(keyWordToken.getText()));

        nodeStack.addLast(fieldASTNode);
        try {
            fieldASTNode.setType(parseType(typeToken));
            fieldASTNode.setName(parseName(nameToken));
            parseSymbol(semicolonToken);
        } finally {
            nodeStack.removeLast();
        }
    }

    /**
     * Parse a define value AST node.
     *
     * @param defineValueToken the define value token.
     */
    private DefineValueASTNode parseDefineValue(final Token defineValueToken) {

        final String text = defineValueToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final DefineValueASTNode defineValueASTNode = new DefineValueASTNode();
        defineValueASTNode.setParent(parent);
        defineValueASTNode.setLine(defineValueToken.getLine());
        defineValueASTNode.setOffset(defineValueToken.getOffset());
        defineValueASTNode.setLength(defineValueToken.getLength());
        defineValueASTNode.setText(text);
        defineValueASTNode.setName(text);

        parent.getChildren().add(defineValueASTNode);

        return defineValueASTNode;
    }

    /**
     * Parse a type AST node.
     *
     * @param typeToken the type token.
     */
    private TypeASTNode parseType(final Token typeToken) {

        final String text = typeToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final TypeASTNode typeASTNode = new TypeASTNode();
        typeASTNode.setParent(parent);
        typeASTNode.setLine(typeToken.getLine());
        typeASTNode.setOffset(typeToken.getOffset());
        typeASTNode.setLength(typeToken.getLength());
        typeASTNode.setText(text);
        typeASTNode.setName(text);

        parent.getChildren().add(typeASTNode);

        return typeASTNode;
    }

    /**
     * Parse a name AST node.
     *
     * @param nameToken the name token.
     */
    private NameASTNode parseName(final Token nameToken) {

        final String text = nameToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final NameASTNode nameASTNode = new NameASTNode();
        nameASTNode.setParent(parent);
        nameASTNode.setLine(nameToken.getLine());
        nameASTNode.setOffset(nameToken.getOffset());
        nameASTNode.setLength(nameToken.getLength());
        nameASTNode.setText(text);
        nameASTNode.setName(text);

        parent.getChildren().add(nameASTNode);

        return nameASTNode;
    }

    /**
     * Parse a string AST node.
     *
     * @param stringToken the string token.
     */
    private void parseStringValue(final Token stringToken) {

        final String text = stringToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final StringASTNode stringASTNode = new StringASTNode();
        stringASTNode.setParent(parent);
        stringASTNode.setLine(stringToken.getLine());
        stringASTNode.setOffset(stringToken.getOffset());
        stringASTNode.setLength(stringToken.getLength());
        stringASTNode.setText(text);

        if (text.equals("\"\"")) {
            stringASTNode.setValue("");
        } else {
            stringASTNode.setValue(text.substring(1, text.length() - 1));
        }

        parent.getChildren().add(stringASTNode);
    }

    /**
     * Parse a symbol AST node.
     *
     * @param symbolToken the symbol token.
     */
    private void parseSymbol(final Token symbolToken) {

        final String text = symbolToken.getText();
        final ASTNode parent = nodeStack.getLast();
        final StringASTNode stringASTNode = new StringASTNode();
        stringASTNode.setParent(parent);
        stringASTNode.setLine(symbolToken.getLine());
        stringASTNode.setOffset(symbolToken.getOffset());
        stringASTNode.setLength(symbolToken.getLength());
        stringASTNode.setText(text);

        parent.getChildren().add(stringASTNode);
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
        } while (token.getType() != type);

        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType);

        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType, final int thirdType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType && token.getType() != thirdType);

        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType, final int thirdType,
                            final int fouthType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType && token.getType() != thirdType &&
                token.getType() != fouthType);

        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
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

        return new Token(charTokenType, offset, line, Character.toString(ch));
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
}
