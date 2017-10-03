package com.jme3.shader.glsl.parser.ast;

public class SingleConditionASTNode extends ConditionASTNode {

    private ASTNode expression;

    public ASTNode getExpression() {
        return expression;
    }

    public void setExpression(final ASTNode expression) {
        this.expression = expression;
    }
}
