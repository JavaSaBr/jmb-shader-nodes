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
    }

    private static final Set<Character> STRING_END_CHARS = new HashSet<>();
    private static final Set<Character> DEFINE_END_CHARS = new HashSet<>();

    private static final Set<Character> SPLIT_CHARS = new HashSet<>();

    static {
        SPLIT_CHARS.add(' ');
        SPLIT_CHARS.add('\n');
        SPLIT_CHARS.add('\t');
        SPLIT_CHARS.add(';');
        STRING_END_CHARS.add('"');
        DEFINE_END_CHARS.addAll(SPLIT_CHARS);
        DEFINE_END_CHARS.add('(');
    }

    public static GLSLParser newInstance() {
        return new GLSLParser();
    }

    /**
     * The stack of ast nodes.
     */
    private final Deque<ASTNode> nodeStack;

    private Set<Character> currentEndChars;

    private Token currentToken;

    private int line;

    private int offset;

    private GLSLParser() {
        this.nodeStack = new ArrayDeque<>();
        this.currentEndChars = SPLIT_CHARS;
    }

    /**
     * Parse the GLSL file.
     *
     * @param path the path to GLSL file.
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

            if(token.getType() == TOKEN_DEFINE) {
                parseDefine(token, content);
            }
            // if it's field declaration
            else if(token.getType() == TOKEN_KEYWORD && FieldType.forKeyWord(token.getText()) != null) {
                parseField(token, content);
            }

        } while (token != Token.EOF_TOKEN);
    }

    private void parseDefine(final Token defineToken, final char[] content) {

        if (defineToken.getText().startsWith("#import")) {
            parseImport(defineToken, content);
            return;
        }

        final Token secondToken = readToken(content);

        if (secondToken.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }
    }

    /**
     * Parse an import AST node.
     *
     * @param defineToken the define token.
     * @param content     the content.
     */
    private void parseImport(final Token defineToken, final char[] content) {

        final Token token = findToken(content, TOKEN_STRING);
        if (token.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

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
     * @param content the content.
     */
    private void parseField(final Token keyWordToken, final char[] content) {

        final Token typeToken = findToken(content, TOKEN_WORD);
        final Token nameToken = findToken(content, TOKEN_WORD);

        if (typeToken.getType() == Token.EOF || nameToken.getType() == Token.EOF) {
            throw new RuntimeException("unexpected EOF token");
        }

        final ASTNode parent = nodeStack.getLast();
        final FieldASTNode fieldASTNode = new FieldASTNode();
        fieldASTNode.setParent(parent);
        fieldASTNode.setLine(keyWordToken.getLine());
        fieldASTNode.setOffset(keyWordToken.getOffset());
        fieldASTNode.setLength(nameToken.getOffset() + nameToken.getLength() - keyWordToken.getOffset());
        fieldASTNode.setText(new String(content, fieldASTNode.getOffset(), fieldASTNode.getLength()));
        fieldASTNode.setFieldType(FieldType.forKeyWord(keyWordToken.getText()));

        nodeStack.addLast(fieldASTNode);
        try {
            fieldASTNode.setType(parseType(typeToken));
            fieldASTNode.setName(parseName(nameToken));
        } finally {
            nodeStack.removeLast();
        }
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

    private Token getCurrentToken() {
        return currentToken;
    }

    private void setCurrentToken(final Token currentToken) {
        this.currentToken = currentToken;
    }

    private void setCurrentEndChars(final Set<Character> currentEndChars) {
        this.currentEndChars = currentEndChars;
    }

    private Set<Character> getCurrentEndChars() {
        return currentEndChars;
    }

    private Token findToken(final char[] content, final int type) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != type);

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType) {

        Token token;
        do {
            token = readToken(content);
        } while (token.getType() != firstType && token.getType() != secondType);

        return token;
    }

    private Token readToken(final char[] content) {

        String text = null;

        while (true) {

            if (offset >= content.length) {
                return Token.EOF_TOKEN;
            }

            final char ch = content[offset++];
            final Set<Character> endChars = getCurrentEndChars();

            if (endChars.contains(ch)) {
                setCurrentEndChars(SPLIT_CHARS);

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

                if(ch == '\n') {
                    line++;
                }

                return Token.SKIP_TOKEN;
            }

            if (text == null) {
                text = Character.toString(ch);
            } else {
                text += ch;
            }

            switch (ch) {
                case '"' : {

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
                case '#' : {
                    setCurrentToken(new Token(TOKEN_DEFINE, line, offset - 1));
                    setCurrentEndChars(DEFINE_END_CHARS);
                    continue;
                }
            }

            if (currentToken != null) {
                continue;
            }

            if (KEYWORDS.contains(text)) {
                return new Token(TOKEN_KEYWORD, offset - text.length(), line, text);
            }
        }
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
