package com.ss.editor.shader.nodes.editor.shader;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.CSS_SHADER_NODES_ROOT;
import static com.ss.rlib.util.ObjectUtils.notNull;
import static java.util.stream.Collectors.toList;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.line.TempLine;
import com.ss.editor.shader.nodes.editor.shader.node.line.VariableLine;
import com.ss.editor.shader.nodes.editor.shader.node.main.AttributeShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.MaterialShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.WorldShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.socket.SocketElement;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
import com.ss.rlib.ui.util.FXUtils;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The container of all shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodesContainer extends ScrollPane {

    @NotNull
    private static final Logger LOGGER = LoggerManager.getLogger(ShaderNodesContainer.class);

    private static final double ZOOM_INTENSITY = 0.0005;

    @NotNull
    private static final ExecutorManager EXECUTOR_MANAGER = ExecutorManager.getInstance();

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
     * The temp line to show a process of binding variables.
     */
    @Nullable
    private TempLine tempLine;

    /**
     * The current scale.
     */
    private double scaleValue;

    public ShaderNodesContainer() {
        this.root = new Pane();
        this.root.prefHeightProperty().bind(heightProperty());
        this.root.prefWidthProperty().bind(widthProperty());
        this.root.widthProperty().addListener((observable, oldValue, newValue) -> invalidateLayout());
        this.root.setOnDragOver(this::handleDragOver);
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
     * Reset all layouts.
     */
    private void invalidateLayout() {
        EXECUTOR_MANAGER.addFXTask(() -> root.getChildren().stream()
                .filter(ShaderNodeElement.class::isInstance)
                .map(ShaderNodeElement.class::cast)
                .forEach(this::resetLayout));
    }

    /**
     * Rest layout of the node.
     *
     * @param node the node.
     */
    private void resetLayout(@NotNull final ShaderNodeElement<?> node) {
        node.resetLayout();
        node.setLayoutX(ThreadLocalRandom.current().nextInt(600));
        node.setLayoutY(ThreadLocalRandom.current().nextInt(600));
    }

    /**
     * Get the temp line to show a process of binding variables.
     *
     * @return the temp line to show a process of binding variables.
     */
    private @Nullable TempLine getTempLine() {
        return tempLine;
    }

    /**
     * Set the temp line to show a process of binding variables.
     *
     * @param tempLine the temp line to show a process of binding variables.
     */
    private void setTempLine(@Nullable final TempLine tempLine) {
        this.tempLine = tempLine;
    }

    /**
     * Sets to show the process of attaching a variable.
     *
     * @param sourceSocket the source socket.
     */
    public void startAttaching(@NotNull final SocketElement sourceSocket) {

        TempLine tempLine = getTempLine();

        if (tempLine != null) {
            FXUtils.removeFromParent(tempLine, root);
            setTempLine(null);
        }

        tempLine = new TempLine(sourceSocket);
        setTempLine(tempLine);

        FXUtils.addToPane(tempLine, root);
    }

    private void handleDragOver(final DragEvent dragEvent) {
        if (dragEvent.getGestureSource() instanceof SocketElement) {
            updateAttaching(dragEvent.getSceneX(), dragEvent.getSceneY());
            dragEvent.consume();
        }
    }

    /**
     * Update the process of attaching a variable.
     *
     * @param sceneX the sceneX.
     * @param sceneY the sceneY.
     */
    public void updateAttaching(final double sceneX, final double sceneY) {

        final TempLine tempLine = getTempLine();
        if (tempLine == null) return;

        final Point2D localCoords = root.sceneToLocal(sceneX, sceneY);
        tempLine.updateEnd(localCoords.getX(), localCoords.getY());
    }

    public void finishAttaching() {

        final TempLine tempLine = getTempLine();
        if (tempLine == null) return;

        FXUtils.removeFromParent(tempLine, root);
        setTempLine(null);
    }

    /**
     * Request to select the node.
     *
     * @param requester the node to select.
     */
    public void requestSelect(@NotNull final ShaderNodeElement<?> requester) {

        final ObservableList<Node> children = root.getChildren();
        children.stream().filter(node -> node != requester)
                .filter(ShaderNodeElement.class::isInstance)
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

        final List<ShaderNodeVariable> uniforms = new ArrayList<>();
        uniforms.addAll(shaderGenerationInfo.getFragmentUniforms());
        uniforms.addAll(shaderGenerationInfo.getVertexUniforms());
        uniforms.addAll(shaderGenerationInfo.getAttributes());

        FXUtils.addToPane(new InputGlobalShaderNodeElement(this, shaderGenerationInfo), root);
        FXUtils.addToPane(new OutputGlobalShaderNodeElement(this, shaderGenerationInfo), root);

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();

        for (final ShaderNode shaderNode : shaderNodes) {
            FXUtils.addToPane(new MainShaderNodeElement(this, shaderNode), root);
        }

        for (final ShaderNodeVariable variable : uniforms) {
            final String nameSpace = variable.getNameSpace();
            if (MaterialShaderNodeElement.NAMESPACE.equals(nameSpace)) {
                FXUtils.addToPane(new MaterialShaderNodeElement(this, variable), root);
            } else if (WorldShaderNodeElement.NAMESPACE.equals(nameSpace)) {
                FXUtils.addToPane(new WorldShaderNodeElement(this, variable), root);
            } else if (AttributeShaderNodeElement.NAMESPACE.equals(nameSpace)) {
                FXUtils.addToPane(new AttributeShaderNodeElement(this, variable), root);
            }
        }

        refreshLines();
    }

    /**
     * Get the current technique.
     *
     * @return the current technique.
     */
    private @NotNull TechniqueDef getTechniqueDef() {
        return notNull(techniqueDef);
    }

    /**
     * Try to find shader node parameter for the variable.
     *
     * @param variable          the variable.
     * @param fromOutputMapping true if the variable is from output mapping.
     * @param input             true if the variable is input variable.
     * @return the parameter or null.
     */
    private @Nullable ShaderNodeParameter findByVariable(@NotNull final ShaderNodeVariable variable,
                                                         final boolean fromOutputMapping, final boolean input) {
        return root.getChildren().stream()
                .filter(ShaderNodeElement.class::isInstance)
                .map(ShaderNodeElement.class::cast)
                .map(shaderNodeElement -> shaderNodeElement.parameterFor(variable, fromOutputMapping, input))
                .filter(Objects::nonNull)
                .findAny().orElse(null);
    }

    /**
     * Refresh all lines.
     */
    private void refreshLines() {

        final ObservableList<Node> children = root.getChildren();
        final List<Node> lines = children.stream()
                .filter(VariableLine.class::isInstance)
                .collect(toList());

        children.removeAll(lines);

        final List<ShaderNode> shaderNodes = getTechniqueDef().getShaderNodes();

        for (final ShaderNode shaderNode : shaderNodes) {

            final List<VariableMapping> inputMapping = shaderNode.getInputMapping();
            final List<VariableMapping> outputMapping = shaderNode.getOutputMapping();

            buildLines(children, inputMapping, false);
            buildLines(children, outputMapping, true);
        }

        final List<Node> toBack = children.stream()
                .filter(VariableLine.class::isInstance)
                .collect(toList());
        toBack.forEach(Node::toBack);
    }

    /**
     * Build relation lines between variables.
     *
     * @param children          the current children.
     * @param mappings          the mappings.
     * @param fromOutputMapping true if it's from output mapping.
     */
    private void buildLines(@NotNull final ObservableList<Node> children, @NotNull final List<VariableMapping> mappings,
                            final boolean fromOutputMapping) {

        for (final VariableMapping variableMapping : mappings) {

            final ShaderNodeVariable leftVariable = variableMapping.getLeftVariable();
            final ShaderNodeVariable rightVariable = variableMapping.getRightVariable();

            final ShaderNodeParameter leftParameter = findByVariable(leftVariable, fromOutputMapping, true);
            final ShaderNodeParameter rightParameter = findByVariable(rightVariable, fromOutputMapping, false);

            if (leftParameter == null || rightParameter == null) {
                LOGGER.warning("not found parameters for " + leftVariable + " and  " + rightVariable);
                continue;
            }

            children.add(new VariableLine(rightParameter, leftParameter));
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
