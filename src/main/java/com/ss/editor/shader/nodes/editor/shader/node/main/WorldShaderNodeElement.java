package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of node element to present world parameters.
 *
 * @author JavaSaBr
 */
public class WorldShaderNodeElement extends OutputVariableShaderNodeElement {

    @NotNull
    public static final String NAMESPACE = "WorldParam";

    /**
     * Convert the uniform binding to shader node variable.
     *
     * @param binding the uniform binding.
     * @return the shader node variable.
     */
    public static @NotNull ShaderNodeVariable toVariable(@NotNull final UniformBinding binding) {
        return new ShaderNodeVariable(binding.getGlslType(), NAMESPACE, binding.name(),
                null, "w_");
    }

    public WorldShaderNodeElement(@NotNull final ShaderNodesContainer container,
                                  @NotNull final ShaderNodeVariable variable) {
        super(container, variable);
    }

    @Override
    protected @NotNull String getTitleText() {
        return "World parameter";
    }

    @Override
    protected @NotNull String getNameSpace() {
        return NAMESPACE;
    }
}
