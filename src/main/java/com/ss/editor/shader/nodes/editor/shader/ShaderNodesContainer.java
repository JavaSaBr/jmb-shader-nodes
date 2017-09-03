package com.ss.editor.shader.nodes.editor.shader;

import com.ss.editor.shader.nodes.ShaderNodesEditorPlugin;
import com.ss.rlib.ui.util.FXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

/**
 * The container of all shader nodes.
 *
 * @author JsavaSaBr
 */
public class ShaderNodesContainer extends ScrollPane {

    private static final double ZOOM_INTENSITY = 0.0005;

    /**
     * The root component to place all nodes.
     */
    @NotNull
    private final Pane root;

    @NotNull
    private final Group zoomNode;

    private double scaleValue = 1;

    public ShaderNodesContainer() {
        this.root = new Pane();
        this.root.prefHeightProperty().bind(heightProperty());
        this.root.prefWidthProperty().bind(widthProperty());
        this.zoomNode = new Group(root);
        this.zoomNode.setOnScroll(this::processEvent);

        FXUtils.addClassTo(root, ShaderNodesEditorPlugin.CSS_SHADER_NODES_ROOT);

        hmaxProperty().addListener((observable, oldValue, newValue) -> setHvalue(newValue.doubleValue() / 2));
        vmaxProperty().addListener((observable, oldValue, newValue) -> setVvalue(newValue.doubleValue() / 2));

        final VBox centered = new VBox(zoomNode);
        centered.setAlignment(Pos.CENTER);

        setPannable(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setContent(centered);
        setHvalue(getHmax() / 2);
        setVvalue(getVmax() / 2);
        setFitToHeight(true);
        setFitToWidth(true);
        updateScale();
    }

    private void updateScale() {
        root.setScaleX(scaleValue);
        root.setScaleY(scaleValue);
    }

    private void processEvent(@NotNull final ScrollEvent event) {

        double zoomFactor = event.getDeltaY() * ZOOM_INTENSITY;

        final Bounds innerBounds = zoomNode.getLayoutBounds();
        final Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        final double newScale = scaleValue + zoomFactor;

        scaleValue = Math.min(Math.max(newScale, 0.2F), 2F);

        updateScale();

        // refresh ScrollPane scroll positions & target bounds
        layout();

        // convert target coordinates to zoomTarget coordinates
        final Point2D posInZoomTarget = root.parentToLocal(zoomNode.parentToLocal(new Point2D(event.getX(), event.getY())));
        // calculate adjustment of scroll position (pixels)
        final Point2D adjustment = root.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

        // convert back to [0, 1] range
        // (too large/small values are automatically corrected by ScrollPane)
        final Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();

        setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
        setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));

        event.consume();
    }
}
