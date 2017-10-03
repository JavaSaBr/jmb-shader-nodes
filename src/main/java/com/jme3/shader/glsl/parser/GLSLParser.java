package com.jme3.shader.glsl.parser;

import com.jme3.shader.glsl.parser.ast.ASTNode;
import com.jme3.shader.glsl.parser.ast.FileASTNode;
import com.jme3.shader.glsl.parser.ast.ImportASTNode;
import com.jme3.shader.glsl.parser.ast.StringASTNode;

import java.util.*;

/**
 * The parser of GLSL code.
 *
 * @author JavaSaBr
 */
public class GLSLParser {

    private static final int TOKEN_DEFINE = 2;
    private static final int TOKEN_STRING = 3;

    private static final Set<Character> STRING_END_CHARS = new HashSet<>();
    private static final Set<Character> DEFINE_END_CHARS = new HashSet<>();

    private static final Set<Character> SPLIT_CHARS = new HashSet<>();
    private static final Map<Character, Integer> CHAR_TO_TOKEN_TYPE = new HashMap<>();

    static {
        SPLIT_CHARS.add(' ');
        SPLIT_CHARS.add('\n');
        SPLIT_CHARS.add('\t');
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
    private Deque<ASTNode> nodeStack;

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

            if(token.getType() == TOKEN_DEFINE) {

            }

        } while (token.getType() != type);

        return token;
    }

    private Token findToken(final char[] content, final int firstType, final int secondType) {

        Token token;
        do {

            token = readToken(content);

            if(token.getType() == TOKEN_DEFINE) {

            }

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

            if("#import".equals(text)) {

            }
        }
    }

    private String getText(final String text) {
        return text == null ? "" : text;
    }
}
