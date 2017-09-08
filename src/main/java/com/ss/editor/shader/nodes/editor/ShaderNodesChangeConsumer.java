package com.ss.editor.shader.nodes.editor;

import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * @author JavaSaBr
 */
public interface ShaderNodesChangeConsumer extends ChangeConsumer {

    /**
     * Get the saved node element locations.
     *
     * @return the saved node element locations.
     */
    @FromAnyThread
    @NotNull Vector2f[] getNodeElementLocations();

    /**
     * Get the saved node element widths.
     *
     * @return the saved node element widths.
     */
    @FromAnyThread
    @NotNull double[] getNodeElementWidths();

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
}
