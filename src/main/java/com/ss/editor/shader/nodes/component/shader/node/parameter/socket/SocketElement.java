package com.ss.editor.shader.nodes.component.shader.node.parameter.socket;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.component.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.node.parameter.ShaderNodeParameter;
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
     * The node parameter element.
     */
    @NotNull
    private final ShaderNodeParameter parameter;

    public SocketElement(@NotNull final ShaderNodeParameter parameter) {
        this.parameter = parameter;
        this.centerXProperty = new SimpleDoubleProperty();
        this.centerYProperty = new SimpleDoubleProperty();

        final ShaderNodeElement<?> nodeElement = parameter.getNodeElement();
        nodeElement.layoutXProperty().addListener(this::updateCenterCoords);
        nodeElement.layoutYProperty().addListener(this::updateCenterCoords);
    }

    /**
     * Get the node parameter element.
     *
     * @return the node parameter element.
     */
    @FXThread
    protected @NotNull ShaderNodeParameter getParameter() {
        return parameter;
    }

    /**
     * Update coords of this socket.
     *
     * @param observable the observable.
     * @param oldValue   the old value.
     * @param newValue   the new value.
     */
    @FXThread
    private void updateCenterCoords(@Nullable final ObservableValue<? extends Number> observable,
                                    @Nullable final Number oldValue, @Nullable final Number newValue) {

        final ShaderNodeParameter parameter = getParameter();
        final ShaderNodeElement<?> nodeElement = parameter.getNodeElement();
        final Parent parent = nodeElement.getParent();

        if (parent == null) {
            return;
        }

        final Point2D scene = localToScene(getWidth() / 2, getHeight() / 2);
        final Point2D local = parent.sceneToLocal(scene.getX(), scene.getY());

        centerXProperty.setValue(local.getX());
        centerYProperty.setValue(local.getY());
    }

    /**
     * @return the center X property.
     */
    @FXThread
    public @NotNull ReadOnlyDoubleProperty centerXPropertyProperty() {
        return centerXProperty;
    }

    /**
     * @return the center Y property.
     */
    @FXThread
    public @NotNull ReadOnlyDoubleProperty centerYPropertyProperty() {
        return centerYProperty;
    }
}
