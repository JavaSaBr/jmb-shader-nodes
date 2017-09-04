package com.ss.editor.shader.nodes.editor.shader;

import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODES_ROOT;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.ShaderNode;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.MainShaderNode;
import com.ss.rlib.ui.util.FXUtils;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The container of all shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodesContainer extends ScrollPane {

    private static final double ZOOM_INTENSITY = 0.0005;

    /**
     * The root component to place all nodes.
     */
    @NotNull
    private final Pane root;

    /**
     * The wrapper of scaled node.
     */
    @NotNull
    private final Group zoomNode;

    /**
     * The current technique.
     */
    @Nullable
    private TechniqueDef techniqueDef;

    /**
     * The current scale.
     */
    private double scaleValue;

    public ShaderNodesContainer() {
        this.root = new Pane();
        this.root.prefHeightProperty().bind(heightProperty());
        this.root.prefWidthProperty().bind(widthProperty());
        this.zoomNode = new Group(root);
        this.zoomNode.setOnScroll(this::processEvent);
        this.scaleValue = 1;

        FXUtils.addClassTo(root, CSS_SHADER_NODES_ROOT);

        final VBox centered = new VBox(zoomNode);
        centered.setAlignment(Pos.CENTER);

        setPannable(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setContent(centered);
        setFitToHeight(true);
        setFitToWidth(true);
        updateScale();
    }

    /**
     * Request to select the node.
     *
     * @param requester the node to select.
     */
    public void requestSelect(@NotNull final ShaderNodeElement<?> requester) {

        final ObservableList<Node> children = root.getChildren();
        children.stream().filter(node -> node != requester)
                .map(node -> (ShaderNodeElement<?>) node)
                .forEach(element -> element.setSelected(false));

        requester.setSelected(true);
    }

    /**
     * Get the root component to place all nodes.
     *
     * @return the root component to place all nodes.
     */
    private @NotNull Pane getRoot() {
        return root;
    }

    /**
     * Show the technique.
     *
     * @param techniqueDef the technique.
     */
    public void show(@NotNull TechniqueDef techniqueDef) {
        this.techniqueDef = techniqueDef;

        final ShaderGenerationInfo shaderGenerationInfo = techniqueDef.getShaderGenerationInfo();
        final Pane root = getRoot();

        FXUtils.addToPane(new InputGlobalShaderNodeElement(this, shaderGenerationInfo), root);
        FXUtils.addToPane(new OutputGlobalShaderNodeElement(this, shaderGenerationInfo), root);

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();

        for (final ShaderNode shaderNode : shaderNodes) {
            FXUtils.addToPane(new MainShaderNode(this, shaderNode), root);
        }
    }

    /**
     * Update scale value.
     */
    private void updateScale() {
        root.setScaleX(scaleValue);
        root.setScaleY(scaleValue);
    }

    /**
     * Handle zoom.
     *
     * @param event the scroll event.
     */
    private void processEvent(@NotNull final ScrollEvent event) {

        double zoomFactor = event.getDeltaY() * ZOOM_INTENSITY;

        final Bounds innerBounds = zoomNode.getLayoutBounds();
        final Bounds viewportBounds = getViewportBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        final double newScale = scaleValue + zoomFactor;

        scaleValue = Math.min(Math.max(newScale, 0.2F), 1F);

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
