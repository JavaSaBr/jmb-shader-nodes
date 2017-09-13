package com.ss.editor.shader.nodes.editor;

import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author JavaSaBr
 */
public interface ShaderNodesChangeConsumer extends ChangeConsumer {

    /**
     * @return the edited material definition.
     */
    @NotNull MaterialDef getMaterialDef();

    @FXThread
    void notifyAddedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping);

    @FXThread
    void notifyRemovedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping);

    @FXThread
    void notifyReplacedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping oldMapping,
                               @NotNull final VariableMapping newMapping);

    @FXThread
    void notifyAddedMatParameter(@NotNull final MatParam matParam, @NotNull Vector2f location);

    @FXThread
    void notifyRemovedMatParameter(@NotNull final MatParam matParam);

    @FXThread
    void notifyAddedAttribute(@NotNull final ShaderNodeVariable variable, @NotNull Vector2f location);

    @FXThread
    void notifyRemovedAttribute(@NotNull final ShaderNodeVariable variable);

    @FXThread
    void notifyAddedWorldParameter(@NotNull final UniformBinding binding, @NotNull Vector2f location);

    @FXThread
    void notifyRemovedWorldParameter(@NotNull final UniformBinding binding);

    @FXThread
    void notifyAddedShaderNode(@NotNull final ShaderNode shaderNode, @NotNull Vector2f location);

    @FXThread
    void notifyRemovedRemovedShaderNode(@NotNull final ShaderNode shaderNode);

    @FXThread
    void notifyChangeState(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location, final double width);

    @FXThread
    void notifyChangeState(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location, final double width);

    @FXThread
    void notifyChangeGlobalNodeState(final boolean input, @NotNull final Vector2f location, final double width);

    @FXThread
    @Nullable Vector2f getLocation(@NotNull final ShaderNode shaderNode);

    @FXThread
    @Nullable Vector2f getLocation(@NotNull final ShaderNodeVariable variable);

    @FXThread
    @Nullable Vector2f getGlobalNodeLocation(final boolean input);

    @FXThread
    double getWidth(@NotNull final ShaderNode shaderNode);

    @FXThread
    double getWidth(@NotNull final ShaderNodeVariable variable);

    @FXThread
    double getGlobalNodeWidth(final boolean input);
}
