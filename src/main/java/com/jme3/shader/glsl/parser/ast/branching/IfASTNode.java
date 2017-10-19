package com.jme3.shader.glsl.parser.ast.branching;

import com.jme3.shader.glsl.parser.ast.ASTNode;
import com.jme3.shader.glsl.parser.ast.BodyASTNode;
import com.jme3.shader.glsl.parser.ast.branching.condition.ConditionASTNode;

/**
 * The node to present an 'if' a statement in the code.
 *
 * @author JavaSaBr
 */
public class IfASTNode extends ASTNode {

    /**
     * The condition.
     */
    private ConditionASTNode condition;

    /**
     * The body.
     */
    private BodyASTNode body;

    /**
     * The else body.
     */
    private BodyASTNode elseBody;

    /**
     * The else node.
     */
    private ASTNode elseNode;

    /**
     * Gets the condition.
     *
     * @return the condition.
     */
    public ConditionASTNode getCondition() {
        return condition;
    }

    /**
     * Sets the condition.
     *
     * @param condition the condition.
     */
    public void setCondition(final ConditionASTNode condition) {
        this.condition = condition;
    }

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

    /**
     * Gets the else body.
     *
     * @return the else body.
     */
    public BodyASTNode getElseBody() {
        return elseBody;
    }

    /**
     * Sets the else body.
     *
     * @param elseBody the else body.
     */
    public void setElseBody(final BodyASTNode elseBody) {
        this.elseBody = elseBody;
    }

    /**
     * Gets the else node.
     *
     * @return the else node.
     */
    public ASTNode getElseNode() {
        return elseNode;
    }

    /**
     * Sets the else node.
     *
     * @param elseNode the else node.
     */
    public void setElseNode(final ASTNode elseNode) {
        this.elseNode = elseNode;
    }
}
