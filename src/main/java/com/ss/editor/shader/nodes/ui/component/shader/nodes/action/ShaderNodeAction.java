package com.ss.editor.shader.nodes.ui.component.shader.nodes.action;

import com.jme3.math.Vector2f;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base implementation of shader nodes action.
 *
 * @author JavaSaBr
 */
public abstract class ShaderNodeAction<T> extends MenuItem {

    /**
     * The shader nodes container.
     */
    @NotNull
    private final ShaderNodesContainer container;

    /**
     * The additional object.
     */
    @NotNull
    private final T object;

    /**
     * The location.
     */
    @NotNull
    private final Vector2f location;

    public ShaderNodeAction(@NotNull final ShaderNodesContainer container, @NotNull final T object,
                            @NotNull final Vector2f location) {
        this.container = container;
        this.object = object;
        this.location = location;

        setOnAction(event -> process());
        setText(getName());

        final Image icon = getIcon();

        if (icon != null) {
            setGraphic(new ImageView(icon));
        }
    }

    /**
     * @return the location.
     */
    @FxThread
    public @NotNull Vector2f getLocation() {
        return location;
    }

    /**
     * @return the shader nodes container.
     */
    @FxThread
    public @NotNull ShaderNodesContainer getContainer() {
        return container;
    }

    /**
     * @return the additional object.
     */
    @FxThread
    public @NotNull T getObject() {
        return object;
    }

    /**
     * Gets name.
     *
     * @return the name of this action.
     */
    @FxThread
    protected abstract @NotNull String getName();

    /**
     * Execute this action.
     */
    @FxThread
    protected void process() {
    }

    /**
     * The icon of this action.
     *
     * @return he icon or null.
     */
    @FxThread
    protected @Nullable Image getIcon() {
        return null;
    }
}
