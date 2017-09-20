package com.ss.editor.shader.nodes.editor;

import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
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
    @FromAnyThread
    @NotNull MaterialDef getMaterialDef();

    /**
     * Get the current preview material.
     *
     * @return the current preview material.
     */
    @FromAnyThread
    @Nullable Material getPreviewMaterial();

    /**
     * Notify about added the variable mapping.
     *
     * @param shaderNode the shader nodes.
     * @param mapping    the variable mapping.
     */
    @FXThread
    void notifyAddedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping);

    /**
     * Notify about removed the variable mapping.
     *
     * @param shaderNode the shader nodes.
     * @param mapping    the variable mapping.
     */
    @FXThread
    void notifyRemovedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping);

    /**
     * Notify about replaced the variable mapping.
     *
     * @param shaderNode the shader nodes.
     * @param oldMapping the old variable mapping.
     * @param newMapping the new variable mapping.
     */
    @FXThread
    void notifyReplacedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping oldMapping,
                               @NotNull final VariableMapping newMapping);

    /**
     * Notify about added the material parameter.
     *
     * @param matParam the material parameter.
     * @param location the location.
     */
    @FXThread
    void notifyAddedMatParameter(@NotNull final MatParam matParam, @NotNull Vector2f location);

    /**
     * Notify about removed the material parameter.
     *
     * @param matParam the material parameter.
     */
    @FXThread
    void notifyRemovedMatParameter(@NotNull final MatParam matParam);

    /**
     * Notify about added the attribute.
     *
     * @param variable the variable of the attribute.
     * @param location the location.
     */
    @FXThread
    void notifyAddedAttribute(@NotNull final ShaderNodeVariable variable, @NotNull Vector2f location);

    /**
     * Notify about removed the attribute.
     *
     * @param variable the variable of the attribute.
     */
    @FXThread
    void notifyRemovedAttribute(@NotNull final ShaderNodeVariable variable);

    /**
     * Notify about added the world parameter.
     *
     * @param binding  the binding.
     * @param location the location.
     */
    @FXThread
    void notifyAddedWorldParameter(@NotNull final UniformBinding binding, @NotNull Vector2f location);

    /**
     * Notify about removed the world parameter.
     *
     * @param binding the binding.
     */
    @FXThread
    void notifyRemovedWorldParameter(@NotNull final UniformBinding binding);

    /**
     * Notify about added the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @param location   the location.
     */
    @FXThread
    void notifyAddedShaderNode(@NotNull final ShaderNode shaderNode, @NotNull Vector2f location);

    /**
     * Notify about removed the shader nodes.
     *
     * @param shaderNode the shader nodes.
     */
    @FXThread
    void notifyRemovedRemovedShaderNode(@NotNull final ShaderNode shaderNode);

    /**
     * Notify about changed state of the shader nodes variable.
     *
     * @param variable the variable.
     * @param location the location.
     * @param width    the width.
     */
    @FXThread
    void notifyChangeState(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location,
                           final double width);

    /**
     * Notify about changed state of the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @param location   the location.
     * @param width      the width.
     */
    @FXThread
    void notifyChangeState(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location, final double width);

    /**
     * Notify about changed state of the global nodes.
     *
     * @param input    true of it's input global nodes.
     * @param location the location.
     * @param width    the width.
     */
    @FXThread
    void notifyChangeGlobalNodeState(final boolean input, @NotNull final Vector2f location, final double width);

    /**
     * Get saved location of the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @return the location or null.
     */
    @FXThread
    @Nullable Vector2f getLocation(@NotNull final ShaderNode shaderNode);

    /**
     * Get saved location of the shader nodes variable.
     *
     * @param variable the shader nodes variable.
     * @return the location or null.
     */
    @FXThread
    @Nullable Vector2f getLocation(@NotNull final ShaderNodeVariable variable);

    /**
     * Get saved location of the global nodes.
     *
     * @param input true if it's input global nodes.
     * @return the location or null.
     */
    @FXThread
    @Nullable Vector2f getGlobalNodeLocation(final boolean input);

    /**
     * Get saved width of the shader nodes.
     *
     * @param shaderNode the shader nodes.
     * @return the width or 0.
     */
    @FXThread
    double getWidth(@NotNull final ShaderNode shaderNode);

    /**
     * Get saved width of the shader nodes variable.
     *
     * @param variable the shader nodes variable.
     * @return the width or null.
     */
    @FXThread
    double getWidth(@NotNull final ShaderNodeVariable variable);

    /**
     * Get saved width of the global nodes.
     *
     * @param input true if it's input global nodes.
     * @return the width or null.
     */
    @FXThread
    double getGlobalNodeWidth(final boolean input);

    /**
     * Notify about added the technique definition.
     *
     * @param techniqueDef the technique definition.
     */
    @FXThread
    void notifyAddedTechnique(@NotNull final TechniqueDef techniqueDef);

    /**
     * Notify about removed the technique definition.
     *
     * @param techniqueDef the technique definition.
     */
    @FXThread
    void notifyRemovedTechnique(@NotNull final TechniqueDef techniqueDef);
}
