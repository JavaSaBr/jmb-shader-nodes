package com.jme3.shader.glsl.parser.ast.branching.condition;

import com.jme3.shader.glsl.parser.GLSLLang;

/**
 * The node to present define condition Is.
 *
 * @author JavaSaBr
 */
public class DefineConditionIsASTNode extends ConditionIsASTNode {

    @Override
    protected String getStringAttributes() {
        return GLSLLang.PR_TYPE_DEFINE;
    }
}
