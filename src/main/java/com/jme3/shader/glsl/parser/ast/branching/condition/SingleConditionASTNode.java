package com.jme3.shader.glsl.parser.ast.branching.condition;

import com.jme3.shader.glsl.parser.ast.ASTNode;

/**
 * The node to present a single condition.
 *
 * @author JavaSaBr
 */
public class SingleConditionASTNode extends ConditionASTNode {

    /**
     * The expression.
     */
    private ASTNode expression;

    /**
     * Gets the expression.
     *
     * @return the expression.
     */
    public ASTNode getExpression() {
        return expression;
    }

    /**
     * Sets the expression.
     *
     * @param expression the expression.
     */
    public void setExpression(final ASTNode expression) {
        this.expression = expression;
    }
}
