package com.jme3.shader.glsl.parser.ast.branching.condition;

import com.jme3.shader.glsl.parser.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;

/**
 * The node to present a multi expressions condition in the code.
 *
 * @author JavaSaBr
 */
public class MultiConditionASTNode extends ConditionASTNode {

    /**
     * The list of expressions.
     */
    private List<ASTNode> expressions;

    protected MultiConditionASTNode() {
        this.expressions = new ArrayList<>();
    }

    /**
     * Gets the list of expressions.
     *
     * @return the list of expressions.
     */
    public List<ASTNode> getExpressions() {
        return expressions;
    }

    /**
     * Adds the expression.
     *
     * @param expression the expression.
     */
    public void addExpression(final ASTNode expression) {
        this.expressions.add(expression);
    }
}
