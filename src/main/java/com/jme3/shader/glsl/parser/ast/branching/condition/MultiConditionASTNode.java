package com.jme3.shader.glsl.parser.ast.branching.condition;

import com.jme3.shader.glsl.parser.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class MultiConditionASTNode extends ConditionASTNode {

    private List<ASTNode> expressions;

    protected MultiConditionASTNode() {
        this.expressions = new ArrayList<>();
    }

    public List<ASTNode> getExpressions() {
        return expressions;
    }

    public void addExpression(final ASTNode expression) {
        this.expressions.add(expression);
    }
}
