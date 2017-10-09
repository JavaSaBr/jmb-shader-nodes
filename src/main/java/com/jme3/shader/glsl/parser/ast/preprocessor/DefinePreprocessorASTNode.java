package com.jme3.shader.glsl.parser.ast.preprocessor;

import com.jme3.shader.glsl.parser.GLSLLang;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.value.ValueASTNode;

/**
 * The node to present a define preprocessor in the code.
 *
 * @author JavaSaBr
 */
public class DefinePreprocessorASTNode extends PreprocessorASTNode {

    /**
     * The definition name.
     */
    private NameASTNode name;

    /**
     * The definition value.
     */
    private ValueASTNode value;

    /**
     * Gets the name of this definition.
     *
     * @return the name of this definition.
     */
    public NameASTNode getName() {
        return name;
    }

    /**
     * Sets the name of this definition.
     *
     * @param name the name of this definition.
     */
    public void setName(final NameASTNode name) {
        this.name = name;
    }

    /**
     * Gets the value of this definition.
     *
     * @return the value of this definition.
     */
    public ValueASTNode getValue() {
        return value;
    }

    /**
     * Sets the value of this definition.
     *
     * @param value the value of this definition.
     */
    public void setValue(final ValueASTNode value) {
        this.value = value;
    }

    @Override
    protected String getStringAttributes() {
        return GLSLLang.PR_DEFINE;
    }
}
