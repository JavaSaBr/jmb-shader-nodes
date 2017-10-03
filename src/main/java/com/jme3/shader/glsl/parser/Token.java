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

    public Token(final int type, final int line, final int offset) {
        this.offset = offset;
        this.line = line;
        this.type = type;
    }

    public Token(int type, String text) {
        this(type, -1, -1);
        this.text = text;
    }

    public Token(int type) {
        this(type, null);
    }

    private Token() {
        this(0, "INVALID");
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
