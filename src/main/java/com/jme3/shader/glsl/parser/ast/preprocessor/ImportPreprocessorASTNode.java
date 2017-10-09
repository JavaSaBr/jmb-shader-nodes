package com.jme3.shader.glsl.parser.ast.preprocessor;

import com.jme3.shader.glsl.parser.GLSLLang;
import com.jme3.shader.glsl.parser.ast.value.StringValueASTNode;

/**
 * The node to present an import preprocessor in the code.
 *
 * @author JavaSaBr
 */
public class ImportPreprocessorASTNode extends PreprocessorASTNode {

    /**
     * The value.
     */
    private StringValueASTNode value;

    /**
     * Gets the value if this import.
     *
     * @return the value if this import.
     */
    public StringValueASTNode getValue() {
        return value;
    }

    /**
     * Sets the value if this import.
     *
     * @param value the value if this import.
     */
    public void setValue(final StringValueASTNode value) {
        this.value = value;
    }

    @Override
    protected String getStringAttributes() {
        return GLSLLang.PR_IMPORT;
    }
}
