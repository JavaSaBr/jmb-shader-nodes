package com.ss.editor.shader.nodes.ui.component.editor.state;

import com.jme3.math.Vector2f;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.ui.component.editor.state.impl.AbstractEditorState;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of storing state of {@link com.jme3.shader.ShaderNodeVariable}.
 *
 * @author JavaSaBr
 */
public class ShaderNodeState extends AbstractEditorState {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1;

    /**
     * The name of a shader nodes.
     */
    @NotNull
    private String name;

    /**
     * The location of a shader nodes.
     */
    @NotNull
    private Vector2f location;

    /**
     * The width of a shader nodes.
     */
    private int width;

    public ShaderNodeState() {
        this.name = "";
        this.location = Vector2f.ZERO;
    }

    public ShaderNodeState(@NotNull String name, @NotNull Vector2f location, int width) {
        this.name = name;
        this.location = location;
        this.width = width;
    }

    /**
     * Get the name of a shader nodes.
     *
     * @return the name of a shader nodes.
     */
    @FxThread
    public @NotNull String getName() {
        return name;
    }

    /**
     * Set the name of a shader nodes.
     *
     * @param name the name of a shader nodes.
     */
    @FxThread
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Get the location of a shader nodes.
     *
     * @return the location of a shader nodes.
     */
    @FxThread
    public @NotNull Vector2f getLocation() {
        return location;
    }

    /**
     * Set the location of a shader nodes.
     *
     * @param location the location of a shader nodes.
     */
    @FxThread
    public void setLocation(@NotNull Vector2f location) {
        this.location = location;
    }

    /**
     * Get the width of a shader nodes.
     *
     * @return the width of a shader nodes.
     */
    @FxThread
    public int getWidth() {
        return width;
    }

    /**
     * Set the width of a shader nodes.
     *
     * @param width the width of a shader nodes.
     */
    @FxThread
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "ShaderNodeState{" + "name='" + name + '\'' + ", location=" + location + ", width=" + width + '}';
    }
}
