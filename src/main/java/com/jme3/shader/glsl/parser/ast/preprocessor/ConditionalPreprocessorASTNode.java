package com.jme3.shader.glsl.parser.ast.preprocessor;

import com.jme3.shader.glsl.parser.ast.BodyASTNode;
import com.jme3.shader.glsl.parser.ast.branching.condition.ConditionASTNode;

public class ConditionalPreprocessorASTNode extends PreprocessorASTNode {

    private ConditionASTNode condition;

    private BodyASTNode body;
    private BodyASTNode elseBody;

    private PreprocessorASTNode elseNode;
    private PreprocessorASTNode endNode;

    public ConditionASTNode getCondition() {
        return condition;
    }

    public void setCondition(final ConditionASTNode condition) {
        this.condition = condition;
    }

    public BodyASTNode getBody() {
        return body;
    }

    public void setBody(final BodyASTNode body) {
        this.body = body;
    }

    public BodyASTNode getElseBody() {
        return elseBody;
    }

    public void setElseBody(final BodyASTNode elseBody) {
        this.elseBody = elseBody;
    }

    public PreprocessorASTNode getElseNode() {
        return elseNode;
    }

    public void setElseNode(final PreprocessorASTNode elseNode) {
        this.elseNode = elseNode;
    }

    public PreprocessorASTNode getEndNode() {
        return endNode;
    }

    public void setEndNode(final PreprocessorASTNode endNode) {
        this.endNode = endNode;
    }
}
