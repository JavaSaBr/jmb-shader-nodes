package com.ss.editor.shader.nodes.editor.state;

import com.jme3.math.Vector2f;
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
     * The name of a shader node.
     */
    @NotNull
    private String name;

    /**
     * The location of a shader node.
     */
    @NotNull
    private Vector2f location;

    /**
     * The width of a shader node.
     */
    private int width;

    public ShaderNodeState() {
        this.name = "";
        this.location = Vector2f.ZERO;
    }

    public ShaderNodeState(@NotNull final String name, @NotNull final Vector2f location, final int width) {
        this.name = name;
        this.location = location;
        this.width = width;
    }

    /**
     * Get the name of a shader node.
     *
     * @return the name of a shader node.
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Set the name of a shader node.
     *
     * @param name the name of a shader node.
     */
    public void setName(@NotNull final String name) {
        this.name = name;
    }

    /**
     * Get the location of a shader node.
     *
     * @return the location of a shader node.
     */
    public @NotNull Vector2f getLocation() {
        return location;
    }

    /**
     * Set the location of a shader node.
     *
     * @param location the location of a shader node.
     */
    public void setLocation(@NotNull final Vector2f location) {
        this.location = location;
    }

    /**
     * Get the width of a shader node.
     *
     * @return the width of a shader node.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the width of a shader node.
     *
     * @param width the width of a shader node.
     */
    public void setWidth(final int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "ShaderNodeState{" + "name='" + name + '\'' + ", location=" + location + ", width=" + width + '}';
    }
}
