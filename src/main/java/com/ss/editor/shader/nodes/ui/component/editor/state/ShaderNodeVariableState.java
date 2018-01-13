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
public class ShaderNodeVariableState extends AbstractEditorState {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1;

    /**
     * The name of a variable.
     */
    @NotNull
    private String name;

    /**
     * The namespace of a variable.
     */
    @NotNull
    private String nameSpace;

    /**
     * The location of a variable.
     */
    @NotNull
    private Vector2f location;

    /**
     * The width of a variable.
     */
    private int width;

    public ShaderNodeVariableState() {
        this.name = "";
        this.nameSpace = "";
        this.location = Vector2f.ZERO;
    }

    public ShaderNodeVariableState(@NotNull final String name, @NotNull final String nameSpace,
                                   @NotNull final Vector2f location, final int width) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.location = location;
        this.width = width;
    }


    /**
     * Get the name of a variable.
     *
     * @return the name of a variable.
     */
    @FxThread
    public @NotNull String getName() {
        return name;
    }

    /**
     * Set the name of a variable.
     *
     * @param name the name of a variable.
     */
    @FxThread
    public void setName(@NotNull final String name) {
        this.name = name;
    }

    /**
     * Get the namespace of a variable.
     *
     * @return the namespace of a variable.
     */
    @FxThread
    public @NotNull String getNameSpace() {
        return nameSpace;
    }

    /**
     * Set the namespace of a variable.
     *
     * @param nameSpace the namespace of a variable.
     */
    @FxThread
    public void setNameSpace(@NotNull final String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /**
     * Get the location of a variable.
     *
     * @return the location of a variable.
     */
    @FxThread
    public @NotNull Vector2f getLocation() {
        return location;
    }

    /**
     * Set the location of a variable.
     *
     * @param location the location of a variable.
     */
    @FxThread
    public void setLocation(@NotNull final Vector2f location) {
        this.location = location;
    }

    /**
     * Get the width of a variable.
     *
     * @return the width of a variable.
     */
    @FxThread
    public int getWidth() {
        return width;
    }

    /**
     * Set the width of a variable.
     *
     * @param width the width of a variable.
     */
    @FxThread
    public void setWidth(final int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "ShaderNodeVariableState{" + "name='" + name + '\'' + ", nameSpace='" + nameSpace + '\'' +
                ", location=" + location + ", width=" + width + '}';
    }
}
