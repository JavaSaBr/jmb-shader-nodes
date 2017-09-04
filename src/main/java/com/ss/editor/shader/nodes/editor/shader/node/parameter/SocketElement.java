package com.ss.editor.shader.nodes.editor.shader.node.parameter;

import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import javafx.beans.property.DoubleProperty;
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
    private final DoubleProperty centerXProperty;

    @NotNull
    private final DoubleProperty centerYProperty;

    @NotNull
    private final ShaderNodeElement<?> nodeElement;

    public SocketElement(@NotNull final ShaderNodeElement<?> nodeElement) {
        this.nodeElement = nodeElement;
        this.centerXProperty = new SimpleDoubleProperty();
        this.centerYProperty = new SimpleDoubleProperty();
        this.nodeElement.layoutXProperty().addListener(this::updateCenterCoords);
        this.nodeElement.layoutYProperty().addListener(this::updateCenterCoords);
        nodeElement.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateCenterCoords(null, null, null);
            }
        });
    }

    private void updateCenterCoords(@Nullable final ObservableValue<? extends Number> observable,
                                    @Nullable final Number oldValue, @Nullable final Number newValue) {

        final Point2D scene = localToScene(getWidth() / 2, getHeight() / 2);
        final Parent parent = nodeElement.getParent();
        final Point2D local = parent.sceneToLocal(scene.getX(), scene.getY());

        centerXProperty.setValue(local.getX());
        centerYProperty.setValue(local.getY());
    }

    public @NotNull DoubleProperty centerXPropertyProperty() {
        return centerXProperty;
    }

    public @NotNull DoubleProperty centerYPropertyProperty() {
        return centerYProperty;
    }
}
