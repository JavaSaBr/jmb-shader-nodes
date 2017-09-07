package com.ss.editor.shader.nodes.editor;

import com.jme3.math.Vector2f;
import com.ss.editor.ui.component.editor.state.impl.EditorMaterialEditorState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * The implementation of a state container for the {@link ShaderNodesFileEditor}.
 *
 * @author JavaSaBr
 */
public class ShaderNodesEditorState extends EditorMaterialEditorState {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 3;

    /**
     * The node element locations.
     */
    @NotNull
    private Vector2f[] nodeElementLocations;

    /**
     * The node element widths.
     */
    @NotNull
    private double[] nodeElementWidths;

    public ShaderNodesEditorState() {
        this.nodeElementLocations = new Vector2f[0];
        this.nodeElementWidths = new double[0];
    }

    /**
     * Update node element locations.
     *
     * @param locations the new node element locations.
     */
    public void updateNodeElementLocations(@NotNull final Vector2f[] locations) {
        final Vector2f[] oldLocations = getNodeElementLocations();
        this.nodeElementLocations = locations;
        if (!Arrays.equals(oldLocations, locations)) notifyChange();
    }

    /**
     * Update node element widths.
     *
     * @param widths the new node element widths.
     */
    public void updateNodeElementWidths(@NotNull final double[] widths) {
        final double[] oldWidths = getNodeElementWidths();
        this.nodeElementWidths = widths;
        if (!Arrays.equals(oldWidths, widths)) notifyChange();
    }

    /**
     * Get the last node element locations.
     *
     * @return the last node element locations.
     */
    public @NotNull Vector2f[] getNodeElementLocations() {
        return nodeElementLocations;
    }

    /**
     * Get the last node element widths.
     *
     * @return the last node element widths.
     */
    public @NotNull double[] getNodeElementWidths() {
        return nodeElementWidths;
    }
}
