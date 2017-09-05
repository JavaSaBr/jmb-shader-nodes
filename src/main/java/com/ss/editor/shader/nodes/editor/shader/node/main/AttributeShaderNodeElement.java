package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of node element to present attribute parameters.
 *
 * @author JavaSaBr
 */
public class AttributeShaderNodeElement extends OutputVariableShaderNodeElement {

    @NotNull
    public static final String NAMESPACE = "Attr";

    public AttributeShaderNodeElement(@NotNull final ShaderNodesContainer container,
                                      @NotNull final ShaderNodeVariable variable) {
        super(container, variable);
    }

    @Override
    protected @NotNull String getTitleText() {
        return "Attribute";
    }

    @Override
    protected @NotNull String getNameSpace() {
        return NAMESPACE;
    }
}
