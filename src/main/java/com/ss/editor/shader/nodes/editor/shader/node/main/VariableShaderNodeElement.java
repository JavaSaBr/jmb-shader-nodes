package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of node element to present shader variable.
 *
 * @author JavaSaBr
 */
public class VariableShaderNodeElement extends ShaderNodeElement<ShaderNodeVariable> {

    public VariableShaderNodeElement(@NotNull final ShaderNodesContainer container,
                                     @NotNull final ShaderNodeVariable variable) {
        super(container, variable);
    }

    @Override
    public ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable, final boolean output) {
        if (!getNameSpace().equals(variable.getNameSpace())) return null;
        return super.parameterFor(variable, output);
    }

    protected @NotNull String getNameSpace() {
        return "unknown";
    }
}
