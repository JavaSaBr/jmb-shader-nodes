package com.ss.editor.shader.nodes.editor;

import com.jme3.math.Vector2f;
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
    @NotNull Vector2f[] getNodeElementLocations();

    /**
     * Get the saved node element widths.
     *
     * @return the saved node element widths.
     */
    @NotNull double[] getNodeElementWidths();
}
