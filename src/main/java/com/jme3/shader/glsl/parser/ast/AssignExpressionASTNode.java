package com.jme3.shader.glsl.parser.ast;

/**
 * The node to present an assign expression in the code.
 *
 * @author JavaSaBr
 */
public class AssignExpressionASTNode extends ASTNode {

    /**
     * The first part.
     */
    private ASTNode firstPart;

    /**
     * The second part.
     */
    private ASTNode secondPart;

    /**
     * Gets the first part.
     *
     * @return the first part.
     */
    public ASTNode getFirstPart() {
        return firstPart;
    }

    /**
     * Sets the first part.
     *
     * @param firstPart the first part.
     */
    public void setFirstPart(final ASTNode firstPart) {
        this.firstPart = firstPart;
    }

    /**
     * Gets the second part.
     *
     * @return the second part.
     */
    public ASTNode getSecondPart() {
        return secondPart;
    }

    /**
     * Sets the second part.
     *
     * @param secondPart the second part.
     */
    public void setSecondPart(final ASTNode secondPart) {
        this.secondPart = secondPart;
    }
}
