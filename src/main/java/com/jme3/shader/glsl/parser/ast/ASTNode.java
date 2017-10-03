package com.jme3.shader.glsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * The base AST node.
 *
 * @author JavaSaBr
 */
public class ASTNode {

    /**
     * THe parent AST node.
     */
    private ASTNode parent;

    /**
     * The children AST nodes.
     */
    private List<ASTNode> children;

    /**
     * The text.
     */
    private String text;

    /**
     * The line.
     */
    private int line;

    /**
     * The offset.
     */
    private int offset;

    /**
     * The length.
     */
    private int length;

    public ASTNode() {
        this.children = new ArrayList<>();
    }

    /**
     * Gets the parent node.
     *
     * @return the parent node.
     */
    public ASTNode getParent() {
        return parent;
    }

    /**
     * Sets the parent node.
     *
     * @param parent the parent node.
     */
    public void setParent(final ASTNode parent) {
        this.parent = parent;
    }

    /**
     * Gets the children nodes.
     *
     * @return the children nodes.
     */
    public List<ASTNode> getChildren() {
        return children;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setLine(final int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
