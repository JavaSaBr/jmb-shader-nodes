package com.jme3.shader.glsl.parser.ast.value;

import com.jme3.shader.glsl.parser.ast.ASTNode;

/**
 * The node to present a value of something.
 *
 * @author JavaSaBr
 */
public class ValueASTNode extends ASTNode {

    /**
     * The string value.
     */
    private String value;

    /**
     * Gets the value.
     *
     * @return the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the value.
     */
    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    protected String getStringAttributes() {
        return getValue();
    }
}
