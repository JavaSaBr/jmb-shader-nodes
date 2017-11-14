package com.ss.editor.shader.nodes.component.shader.nodes.main;

import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.component.shader.nodes.action.remove.RemoveWorldParamShaderNodeAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of nodes element to present world parameters.
 *
 * @author JavaSaBr
 */
public class WorldShaderNodeElement extends OutputVariableShaderNodeElement {

    @NotNull
    public static final String NAMESPACE = "WorldParam";

    /**
     * Convert the uniform binding to shader nodes variable.
     *
     * @param binding the uniform binding.
     * @return the shader nodes variable.
     */
    public static @NotNull ShaderNodeVariable toVariable(@NotNull final UniformBinding binding) {
        return new ShaderNodeVariable(binding.getGlslType(), NAMESPACE, binding.name(),
                null, "g_");
    }

    public WorldShaderNodeElement(@NotNull final ShaderNodesContainer container,
                                  @NotNull final ShaderNodeVariable variable) {
        super(container, variable);
    }

    @Override
    @FXThread
    protected @NotNull String getTitleText() {
        return PluginMessages.NODE_ELEMENT_WORLD_PARAMETER;
    }

    @Override
    @FXThread
    protected @NotNull String getNameSpace() {
        return NAMESPACE;
    }

    @Override
    @FXThread
    public @Nullable ShaderNodeAction<?> getDeleteAction() {
        return new RemoveWorldParamShaderNodeAction(getContainer(), getObject(),
                new Vector2f((float) getLayoutX(), (float) getLayoutY()));
    }
}
