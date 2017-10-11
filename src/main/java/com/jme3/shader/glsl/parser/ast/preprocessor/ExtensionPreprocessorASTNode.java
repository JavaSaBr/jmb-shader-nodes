package com.jme3.shader.glsl.parser.ast.preprocessor;

import com.jme3.shader.glsl.parser.GLSLLang;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.value.ExtensionStatusValueASTNode;

/**
 * The node to present an extension preprocessor in the code.
 *
 * @author JavaSaBr
 */
public class ExtensionPreprocessorASTNode extends PreprocessorASTNode {

    /**
     * The extension.
     */
    private NameASTNode extension;

    /**
     * The extension status.
     */
    private ExtensionStatusValueASTNode status;

    /**
     * Gets the extension.
     *
     * @return the extension.
     */
    public NameASTNode getExtension() {
        return extension;
    }

    /**
     * Sets the extension.
     *
     * @param extension the extension.
     */
    public void setExtension(final NameASTNode extension) {
        this.extension = extension;
    }

    /**
     * Gets the extension status.
     *
     * @return the extension status.
     */
    public ExtensionStatusValueASTNode getStatus() {
        return status;
    }

    /**
     * Sets the extension status.
     *
     * @param status the extension status.
     */
    public void setStatus(final ExtensionStatusValueASTNode status) {
        this.status = status;
    }

    @Override
    protected String getStringAttributes() {
        return GLSLLang.PR_TYPE_EXTENSION;
    }
}
