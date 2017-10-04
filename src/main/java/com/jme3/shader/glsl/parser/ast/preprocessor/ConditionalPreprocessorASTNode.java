package com.jme3.shader.glsl.parser.ast.preprocessor;

import com.jme3.shader.glsl.parser.ast.ASTNode;
import com.jme3.shader.glsl.parser.ast.branching.condition.ConditionASTNode;

public class ConditionalPreprocessorASTNode extends PreprocessorASTNode {

    private ConditionASTNode condition;

    private PreprocessorBodyASTNode body;

    private ASTNode elseNode;

    public ConditionASTNode getCondition() {
        return condition;
    }

    public void setCondition(final ConditionASTNode condition) {
        this.condition = condition;
    }

    public PreprocessorBodyASTNode getBody() {
        return body;
    }

    public void setBody(final PreprocessorBodyASTNode body) {
        this.body = body;
    }

    public ASTNode getElseNode() {
        return elseNode;
    }

    public void setElseNode(final ASTNode elseNode) {
        this.elseNode = elseNode;
    }
}
