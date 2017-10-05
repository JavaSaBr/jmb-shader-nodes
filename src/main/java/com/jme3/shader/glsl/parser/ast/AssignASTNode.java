package com.jme3.shader.glsl.parser.ast;

/**
 * The node to present assign value.
 *
 * @author JavaSaBr
 */
public class AssignASTNode extends ASTNode {

    /**
     * The assigned value.
     */
    private ASTNode value;

    /**
     * Gets the assigned value.
     *
     * @return the assigned value.
     */
    public ASTNode getValue() {
        return value;
    }

    /**
     * Sets the assigned value.
     *
     * @param value the assigned value.
     */
    public void setValue(final ASTNode value) {
        this.value = value;
    }
}
