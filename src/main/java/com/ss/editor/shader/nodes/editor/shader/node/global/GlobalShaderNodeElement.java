package com.ss.editor.shader.nodes.editor.shader.node.global;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of a global parameters.
 *
 * @author JavaSaBr
 */
public class GlobalShaderNodeElement extends ShaderNodeElement<ShaderGenerationInfo> {

    public GlobalShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderGenerationInfo object) {
        super(container, object);
    }

    @Override
    public ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable, final boolean output) {
        if (!"Global".equals(variable.getNameSpace())) return null;
        return super.parameterFor(variable, output);
    }
}
