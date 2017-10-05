package com.jme3.shader.glsl.parser.ast;

import com.jme3.shader.glsl.parser.ast.util.ASTUtils;

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

    /**
     * Adds the new child to this node.
     *
     * @param child the new child.
     */
    public void addChild(final ASTNode child) {
        children.add(child);
    }

    /**
     * Removes the old child from this nod.e
     *
     * @param child the old child.
     */
    public void removeChild(final ASTNode child) {
        children.remove(child);
    }

    /**
     * Sets the length.
     *
     * @param length the length.
     */
    public void setLength(final int length) {
        this.length = length;
    }

    /**
     * Gets the length.
     *
     * @return the length.
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the line.
     *
     * @param line the line.
     */
    public void setLine(final int line) {
        this.line = line;
    }

    /**
     * Gets the line.
     *
     * @return the line.
     */
    public int getLine() {
        return line;
    }

    /**
     * Sets the offset.
     *
     * @param offset the offset.
     */
    public void setOffset(final int offset) {
        this.offset = offset;
    }

    /**
     * Gets the offset.
     *
     * @return the offset.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the text.
     *
     * @param text the text.
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Gets the text.
     *
     * @return the text.
     */
    public String getText() {
        return text;
    }

    /**
     * Try to find the last node of the type.
     *
     * @param type the type.
     * @param <T>  the node type.
     * @return the last node or null.
     */
    public <T extends ASTNode> T getLastNode(final Class<T> type) {

        final List<ASTNode> children = getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {

            final ASTNode child = children.get(i);
            final T lastNode = child.getLastNode(type);

            if (lastNode != null) {
                return lastNode;
            } else if (type.isInstance(child)) {
                return type.cast(child);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return toString(this, 0);
    }

    /**
     * Build a string presentation of the node for the level.
     *
     * @param node  the node.
     * @param level the level.
     * @return the string presentation.
     */
    protected String toString(final ASTNode node, final int level) {

        final String indent = ASTUtils.getIndent(level);
        final Class<? extends ASTNode> type = node.getClass();
        final String typeName = type.getSimpleName();

        String result = indent + "-" + typeName + ": " + node.getStringAttributes();

        final List<ASTNode> children = node.getChildren();
        if (children.isEmpty()) {
            return result;
        }

        for (final ASTNode child : children) {
            final String childString = toString(child, level + 1);
            result += ("\n" + childString);
        }

        return result;
    }

    /**
     * Gets the string attributes of this node.
     *
     * @return the string attributes.
     */
    protected String getStringAttributes() {
        return "";
    }
}
