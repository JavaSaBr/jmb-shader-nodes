package com.jme3.shader.glsl.parser.ast.branching.condition;

import com.jme3.shader.glsl.parser.ast.ASTNode;

public class SingleConditionASTNode extends ConditionASTNode {

    private ASTNode expression;

    public ASTNode getExpression() {
        return expression;
    }

    public void setExpression(final ASTNode expression) {
        this.expression = expression;
    }
}
