package com.jme3.shader.glsl.parser.ast;

public class ConditionalPreprocessorASTNode extends PreprocessorASTNode {

    private ConditionASTNode conditionNode;

    private ASTNode elseNode;

    public ConditionASTNode getConditionNode() {
        return conditionNode;
    }

    public void setConditionNode(final ConditionASTNode conditionNode) {
        this.conditionNode = conditionNode;
    }

    public ASTNode getElseNode() {
        return elseNode;
    }

    public void setElseNode(final ASTNode elseNode) {
        this.elseNode = elseNode;
    }
}
