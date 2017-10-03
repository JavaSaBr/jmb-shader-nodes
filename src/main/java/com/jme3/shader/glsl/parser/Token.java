package com.jme3.shader.glsl.parser;

public class Token {

    final static int EOF = -1;
    final static int INVALID = 0;
    final static int SKIP = 1;

    final static Token EOF_TOKEN = new Token(EOF, "EOF");
    final static Token INVALID_TOKEN = new Token(INVALID, "INVALID");
    final static Token SKIP_TOKEN = new Token(SKIP, "SKIP");

    private String text;

    private int offset;
    private int length;
    private int line;
    private int type;

    private Token() {
        this(0, "INVALID");
    }

    public Token(final int type) {
        this(type, null);
    }

    public Token(final int type, final String text) {
        this(type, -1, -1, text);
    }

    public Token(final int type, final int line, final int offset) {
        this(type, offset, line, null);
    }

    public Token(final int type, final int offset, final int line, final String text) {
        this.text = text;
        this.offset = offset;
        this.length = text == null ? -1 : text.length();
        this.line = line;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public int getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "Token{" + "text='" + text + '\'' + ", offset=" + offset + ", length=" + length + ", line=" + line +
                ", type=" + type + '}';
    }
}
