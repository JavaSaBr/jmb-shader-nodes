package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The socket element on UI.
 *
 * @author JavaSaBr
 */
public class SocketElement extends Pane {

    @NotNull
    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

    /**
     * The center X property.
     */
    @NotNull
    private final DoubleProperty centerXProperty;

    /**
     * The center Y property.
     */
    @NotNull
    private final DoubleProperty centerYProperty;

    /**
     * The nodes parameter element.
     */
    @NotNull
    private final ShaderNodeParameter parameter;

    public SocketElement(@NotNull ShaderNodeParameter parameter) {
        this.parameter = parameter;
        this.centerXProperty = new SimpleDoubleProperty();
        this.centerYProperty = new SimpleDoubleProperty();

        var element = parameter.getNodeElement();
        element.layoutXProperty().addListener(this::updateCenterCoords);
        element.layoutYProperty().addListener(this::updateCenterCoords);
        element.heightProperty().addListener((observable, oldValue, newValue) ->
                EXECUTOR_MANAGER.addFxTask(this::updateCenterCoords));
    }

    /**
     * Get the nodes parameter element.
     *
     * @return the nodes parameter element.
     */
    @FxThread
    protected @NotNull ShaderNodeParameter getParameter() {
        return parameter;
    }

    /**
     * Update coords of this socket.
     */
    @FxThread
    public void updateCenterCoords() {
        updateCenterCoords(null, null, null);
    }

    /**
     * Update coords of this socket.
     *
     * @param observable the observable.
     * @param oldValue   the old value.
     * @param newValue   the new value.
     */
    @FxThread
    private void updateCenterCoords(
        @Nullable ObservableValue<? extends Number> observable,
        @Nullable Number oldValue,
        @Nullable Number newValue
    ) {

        var parameter = getParameter();
        var nodeElement = parameter.getNodeElement();
        var parent = nodeElement.getParent();
        if (parent == null) {
            return;
        }

        var scene = localToScene(getWidth() / 2, getHeight() / 2);
        var local = parent.sceneToLocal(scene.getX(), scene.getY());

        centerXProperty.setValue(local.getX());
        centerYProperty.setValue(local.getY());
    }

    /**
     * Get the center X property.
     *
     * @return the center X property.
     */
    @FxThread
    public @NotNull ReadOnlyDoubleProperty centerXPropertyProperty() {
        return centerXProperty;
    }

    /**
     * Get the center Y property.
     *
     * @return the center Y property.
     */
    @FxThread
    public @NotNull ReadOnlyDoubleProperty centerYPropertyProperty() {
        return centerYProperty;
    }
}
