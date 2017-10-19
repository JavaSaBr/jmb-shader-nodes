package com.jme3.shader.glsl.parser.ast.branching;

import com.jme3.shader.glsl.parser.ast.ASTNode;
import com.jme3.shader.glsl.parser.ast.BodyASTNode;

/**
 * The node to present an 'for' a statement in the code.
 *
 * @author JavaSaBr
 */
public class ForASTNode extends ASTNode {

    /**
     * The body.
     */
    private BodyASTNode body;

    /**
     * Gets the body.
     *
     * @return the body.
     */
    public BodyASTNode getBody() {
        return body;
    }

    /**
     * Sets the body.
     *
     * @param body the body.
     */
    public void setBody(final BodyASTNode body) {
        this.body = body;
    }
}
