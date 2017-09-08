package com.ss.editor.shader.nodes.editor;

import com.jme3.math.Vector2f;
import com.ss.editor.ui.component.editor.state.impl.EditorMaterialEditorState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of a state container for the {@link ShaderNodesFileEditor}.
 *
 * @author JavaSaBr
 */
public class ShaderNodesEditorState extends EditorMaterialEditorState {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 4;

    @NotNull
    private static final double[] EMPTY_WIDTHS = new double[0];

    @NotNull
    private static final Vector2f[] EMPTY_LOCATIONS = new Vector2f[0];

    /**
     * The node element locations.
     */
    @NotNull
    private Map<String, Vector2f[]> nodeElementLocations;

    /**
     * The node element widths.
     */
    @NotNull
    private Map<String, double[]> nodeElementWidths;

    public ShaderNodesEditorState() {
        this.nodeElementLocations = new HashMap<>();
        this.nodeElementWidths = new HashMap<>();
    }

    /**
     * Update node element locations.
     *
     * @param techName  the technique name.
     * @param locations the new node element locations.
     */
    public void updateNodeElementLocations(@NotNull final String techName, @NotNull final Vector2f[] locations) {
        final Vector2f[] oldLocations = getNodeElementLocations(techName);
        this.nodeElementLocations.put(techName, locations);
        if (!Arrays.equals(oldLocations, locations)) notifyChange();
    }

    /**
     * Update node element widths.
     *
     * @param techName the technique name.
     * @param widths   the new node element widths.
     */
    public void updateNodeElementWidths(@NotNull final String techName, @NotNull final double[] widths) {
        final double[] oldWidths = getNodeElementWidths(techName);
        this.nodeElementWidths.put(techName, widths);
        if (!Arrays.equals(oldWidths, widths)) notifyChange();
    }

    /**
     * Get the last node element locations.
     *
     * @param techName the technique name.
     * @return the last node element locations.
     */
    public @NotNull Vector2f[] getNodeElementLocations(@NotNull final String techName) {
        final Vector2f[] locations = nodeElementLocations.get(techName);
        return locations == null ? EMPTY_LOCATIONS : locations;
    }

    /**
     * Get the last node element widths.
     *
     * @param techName the technique name.
     * @return the last node element widths.
     */
    public @NotNull double[] getNodeElementWidths(@NotNull final String techName) {
        final double[] widths = nodeElementWidths.get(techName);
        return widths == null ? EMPTY_WIDTHS : widths;
    }
}
