package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.shader.ShaderNodeVariable;
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
