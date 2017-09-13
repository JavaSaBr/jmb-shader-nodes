package com.ss.editor.shader.nodes.editor.shader;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODES_ROOT;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.hasInMappingByRightVar;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.hasOutMappingByLeftVar;
import static com.ss.rlib.util.ObjectUtils.notNull;
import static java.util.stream.Collectors.toList;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.*;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.action.*;
import com.ss.editor.shader.nodes.editor.shader.node.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.line.TempLine;
import com.ss.editor.shader.nodes.editor.shader.node.line.VariableLine;
import com.ss.editor.shader.nodes.editor.shader.node.main.*;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.socket.SocketElement;
import com.ss.editor.shader.nodes.util.MaterialDefUtils;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * All current node elements.
     */
    @NotNull
    private final Array<ShaderNodeElement<?>> nodeElements;

    /**
     * The change consumer.
     */
    @NotNull
    private final ShaderNodesChangeConsumer changeConsumer;

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
     * The context menu.
     */
    @NotNull
    private final ContextMenu contextMenu;

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

    public ShaderNodesContainer(@NotNull final ShaderNodesChangeConsumer changeConsumer) {
        this.changeConsumer = changeConsumer;
        this.nodeElements = ArrayFactory.newArray(ShaderNodeElement.class);
        this.root = new Pane();
        this.root.prefHeightProperty().bind(heightProperty());
        this.root.prefWidthProperty().bind(widthProperty());
        this.root.setOnDragOver(this::handleDragOver);
        this.root.setOnMouseClicked(this::handleMouseClicked);
        this.zoomNode = new Group(root);
        this.zoomNode.setOnScroll(this::handleScrollEvent);
        this.scaleValue = 1;
        this.contextMenu = new ContextMenu();
        this.root.setOnContextMenuRequested(this::handleContextMenuEvent);
        this.root.widthProperty().addListener((observable, oldValue, newValue) -> resetLayout());

        FXUtils.addClassTo(root, SHADER_NODES_ROOT);

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
     * Get all current node elements.
     *
     * @return all current node elements.
     */
    public @NotNull Array<ShaderNodeElement<?>> getNodeElements() {
        return nodeElements;
    }

    /**
     * @return the change consumer.
     */
    public @NotNull ShaderNodesChangeConsumer getChangeConsumer() {
        return changeConsumer;
    }

    /**
     * Reset all layouts.
     */
    @FXThread
    private void invalidateSizes() {

        final ShaderNodesChangeConsumer consumer = getChangeConsumer();
        final Array<ShaderNodeElement<?>> nodeElements = getNodeElements();

        for (final ShaderNodeElement<?> nodeElement : nodeElements) {
            nodeElement.resetLayout();

            final Object object = nodeElement.getObject();
            final double width;

            if (object instanceof ShaderNode) {
                width = consumer.getWidth((ShaderNode) object);
            } else if (object instanceof ShaderNodeVariable) {
                width = consumer.getWidth((ShaderNodeVariable) object);
            } else if (nodeElement instanceof InputGlobalShaderNodeElement) {
                width = consumer.getGlobalNodeWidth(true);
            } else if (nodeElement instanceof OutputGlobalShaderNodeElement) {
                width = consumer.getGlobalNodeWidth(false);
            } else {
                width = 0D;
            }

            if (width == 0D) {
                continue;
            }

            nodeElement.setPrefWidth(width);
            nodeElement.resize(width, nodeElement.getHeight());
        }

        EXECUTOR_MANAGER.addFXTask(this::invalidateLayout);
    }

    /**
     * Update all layouts.
     */
    @FXThread
    private void invalidateLayout() {

        final ShaderNodesChangeConsumer consumer = getChangeConsumer();
        final Array<ShaderNodeElement<?>> nodeElements = getNodeElements();

        for (final ShaderNodeElement<?> nodeElement : nodeElements) {
            nodeElement.resetLayout();

            final Object object = nodeElement.getObject();
            final Vector2f location;

            if (object instanceof ShaderNode) {
                location = consumer.getLocation((ShaderNode) object);
            } else if (object instanceof ShaderNodeVariable) {
                location = consumer.getLocation((ShaderNodeVariable) object);
            } else if (nodeElement instanceof InputGlobalShaderNodeElement) {
                location = consumer.getGlobalNodeLocation(true);
            } else if (nodeElement instanceof OutputGlobalShaderNodeElement) {
                location = consumer.getGlobalNodeLocation(false);
            } else {
                location = null;
            }

            if (location == null) {
                continue;
            }

            nodeElement.setLayoutX(location.getX());
            nodeElement.setLayoutY(location.getY());
        }
    }

    /**
     * Reset all layouts.
     */
    @FXThread
    private void resetLayout() {
        final Array<ShaderNodeElement<?>> nodeElements = getNodeElements();
        nodeElements.forEach(ShaderNodeElement::resetLayout);
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

    /**
     * Handle the drag over event.
     *
     * @param dragEvent the drag over event.
     */
    private void handleDragOver(@NotNull final DragEvent dragEvent) {
        if (dragEvent.getGestureSource() instanceof SocketElement) {
            updateAttaching(dragEvent.getSceneX(), dragEvent.getSceneY());
            dragEvent.consume();
        }
    }

    /**
     * Handle the mouse event.
     *
     * @param event the mouse event.
     */
    private void handleMouseClicked(@NotNull final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    /**
     * Handle the context menu event.
     *
     * @param event the context menu event.
     */
    private void handleContextMenuEvent(@NotNull final ContextMenuEvent event) {

        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }

        final Vector2f location = new Vector2f((float) event.getX(), (float) event.getY());
        final TechniqueDef techniqueDef = getTechniqueDef();
        final MaterialDef materialDef = getChangeConsumer().getMaterialDef();

        final ObservableList<MenuItem> items = contextMenu.getItems();
        items.clear();

        final Menu menu = new Menu("Add");
        menu.getItems().addAll(new AddMaterialParamShaderNodeAction(this, materialDef, location),
                new AddMaterialTextureParamShaderNodeAction(this, materialDef, location),
                new AddWorldParameterShaderNodeAction(this, techniqueDef, location),
                new AddAttributeShaderNodeAction(this, techniqueDef, location),
                new AddNodeShaderNodeAction(this, techniqueDef, location));

        items.addAll(menu);

        contextMenu.show(root, event.getScreenX(), event.getScreenY());
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
    public void show(@NotNull final TechniqueDef techniqueDef) {
        if (techniqueDef.equals(this.techniqueDef)) return;

        this.techniqueDef = techniqueDef;

        final ShaderGenerationInfo shaderGenerationInfo = techniqueDef.getShaderGenerationInfo();
        final Pane root = getRoot();

        final Array<ShaderNodeElement<?>> nodeElements = getNodeElements();
        nodeElements.clear();

        final ShaderNodesChangeConsumer consumer = getChangeConsumer();
        final MaterialDef materialDef = consumer.getMaterialDef();

        final Map<String, MatParam> matParams = MaterialDefUtils.getMatParams(materialDef);
        final List<UniformBinding> worldBindings = techniqueDef.getWorldBindings();

        final List<ShaderNodeVariable> uniforms = new ArrayList<>();

        for (final MatParam matParam : matParams.values()) {
            uniforms.add(MaterialShaderNodeElement.toVariable(matParam));
        }

        for (final UniformBinding worldBinding : worldBindings) {
            uniforms.add(WorldShaderNodeElement.toVariable(worldBinding));
        }

        uniforms.addAll(shaderGenerationInfo.getAttributes());

        nodeElements.add(new InputGlobalShaderNodeElement(this, shaderGenerationInfo));
        nodeElements.add(new OutputGlobalShaderNodeElement(this, shaderGenerationInfo));

        for (final ShaderNodeVariable variable : uniforms) {
            final ShaderNodeElement<?> nodeElement = createNodeElement(variable);
            if (nodeElement != null) {
                nodeElements.add(nodeElement);
            }
        }

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();

        for (final ShaderNode shaderNode : shaderNodes) {
            final ShaderNodeDefinition definition = shaderNode.getDefinition();
            if (definition.getType() == Shader.ShaderType.Vertex) {
                nodeElements.add(new VertexShaderNodeElement(this, shaderNode));
            } else if (definition.getType() == Shader.ShaderType.Fragment) {
                nodeElements.add(new FragmentShaderNodeElement(this, shaderNode));
            }
        }

        nodeElements.forEach(root.getChildren(), (nodeElement, nodes) -> nodes.add(nodeElement));
        refreshLines();

        EXECUTOR_MANAGER.addFXTask(this::invalidateSizes);
    }

    /**
     * Find all shader nodes with left output variables.
     *
     * @param leftVariable the left variable.
     * @return the list of nodes.
     */
    @FXThread
    public @NotNull List<ShaderNode> findWithLeftOutputVar(@NotNull final ShaderNodeVariable leftVariable) {
        return root.getChildren().stream()
                .filter(MainShaderNodeElement.class::isInstance)
                .map(MainShaderNodeElement.class::cast)
                .map(ShaderNodeElement::getObject)
                .filter(shaderNode -> hasOutMappingByLeftVar(shaderNode, leftVariable))
                .collect(toList());
    }

    /**
     * Find all shader nodes with right input variables.
     *
     * @param rightVariable the right variable.
     * @param type          the type.
     * @return the list of nodes.
     */
    @FXThread
    public @NotNull List<ShaderNode> findWithRightInputVar(@NotNull final ShaderNodeVariable rightVariable,
                                                           @NotNull final Class<? extends MainShaderNodeElement> type) {
        return root.getChildren().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .map(ShaderNodeElement::getObject)
                .filter(shaderNode -> hasInMappingByRightVar(shaderNode, rightVariable))
                .collect(toList());
    }

    /**
     * Find a node element by the variable.
     *
     * @param variable the variable.
     * @return the node element or null.
     */
    @FXThread
    private @Nullable ShaderNodeElement<?> findNodeElementByVariable(@NotNull final ShaderNodeVariable variable) {
        return (ShaderNodeElement<?>) root.getChildren().stream()
                    .filter(ShaderNodeElement.class::isInstance)
                    .map(ShaderNodeElement.class::cast)
                    .filter(element -> element.getObject().equals(variable))
                    .findAny().orElse(null);
    }

    /**
     * Find a node element by the object.
     *
     * @param object the object.
     * @return the node element or null.
     */
    @FXThread
    private @Nullable ShaderNodeElement<?> findNodeElementByObject(@NotNull final Object object) {
        return (ShaderNodeElement<?>) root.getChildren().stream()
                .filter(ShaderNodeElement.class::isInstance)
                .map(ShaderNodeElement.class::cast)
                .filter(element -> element.getObject().equals(object))
                .findAny().orElse(null);
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

    @FXThread
    public void addMatParam(@NotNull final MatParam matParam, @NotNull final Vector2f location) {
        addNodeElement(MaterialShaderNodeElement.toVariable(matParam), location);
    }

    @FXThread
    public void addWorldParam(@NotNull final UniformBinding binding, @NotNull final Vector2f location) {
        addNodeElement(WorldShaderNodeElement.toVariable(binding), location);
    }

    @FXThread
    public void addShaderNode(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location) {

        final ShaderNodeDefinition definition = shaderNode.getDefinition();
        final MainShaderNodeElement nodeElement;

        if (definition.getType() == Shader.ShaderType.Vertex) {
            nodeElement = new VertexShaderNodeElement(this, shaderNode);
        } else if (definition.getType() == Shader.ShaderType.Fragment) {
            nodeElement = new FragmentShaderNodeElement(this, shaderNode);
        } else {
            throw new RuntimeException("unknown shader node type " + shaderNode);
        }

        addNodeElement(nodeElement, location);
    }

    @FXThread
    public void addNodeElement(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {

        final ShaderNodeElement<?> nodeElement = createNodeElement(variable);
        if (nodeElement == null) {
            return;
        }

        addNodeElement(nodeElement, location);
    }

    @FXThread
    private void addNodeElement(@NotNull final ShaderNodeElement<?> nodeElement, @NotNull final Vector2f location) {

        nodeElement.setLayoutX(location.getX());
        nodeElement.setLayoutY(location.getY());

        final ObservableList<Node> children = root.getChildren();
        children.add(nodeElement);

        final Array<ShaderNodeElement<?>> nodeElements = getNodeElements();
        nodeElements.add(nodeElement);
    }

    @FXThread
    public void removeMatParam(@NotNull final MatParam matParam) {
        removeNodeElement(MaterialShaderNodeElement.toVariable(matParam));
    }

    @FXThread
    public void removeWorldParam(@NotNull final UniformBinding binding) {
        removeNodeElement(WorldShaderNodeElement.toVariable(binding));
    }

    @FXThread
    public void removeShaderNode(@NotNull final ShaderNode shaderNode) {
        final ShaderNodeElement<?> nodeElement = findNodeElementByObject(shaderNode);
        if (nodeElement == null) return;
        removeNodeElement(nodeElement);
    }

    /**
     * Remove an element of the variable.
     *
     * @param variable the variable.
     */
    @FXThread
    public void removeNodeElement(@NotNull final ShaderNodeVariable variable) {

        final ShaderNodeElement<?> nodeElement = findNodeElementByVariable(variable);
        if (nodeElement == null) {
            return;
        }

        removeNodeElement(nodeElement);
    }

    @FXThread
    private void removeNodeElement(@NotNull final ShaderNodeElement<?> nodeElement) {
        root.getChildren().remove(nodeElement);
        getNodeElements().slowRemove(nodeElement);
        refreshLines();
    }

    /**
     * Create a node element for the variable.
     *
     * @param variable the variable.
     * @return the node element.
     */
    private @Nullable ShaderNodeElement<?> createNodeElement(@NotNull ShaderNodeVariable variable) {

        ShaderNodeElement<?> nodeElement = null;

        final String nameSpace = variable.getNameSpace();
        if (MaterialShaderNodeElement.NAMESPACE.equals(nameSpace)) {
            nodeElement = new MaterialShaderNodeElement(this, variable);
        } else if (WorldShaderNodeElement.NAMESPACE.equals(nameSpace)) {
            nodeElement = new WorldShaderNodeElement(this, variable);
        } else if (AttributeShaderNodeElement.NAMESPACE.equals(nameSpace)) {
            nodeElement = new AttributeShaderNodeElement(this, variable);
        }
        return nodeElement;
    }

    /**
     * Get the current technique.
     *
     * @return the current technique.
     */
    public @NotNull TechniqueDef getTechniqueDef() {
        return notNull(techniqueDef);
    }

    /**
     * Refresh all lines.
     */
    public void refreshLines() {

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
    private void handleScrollEvent(@NotNull final ScrollEvent event) {

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

    /**
     * Notify about moved node element.
     *
     * @param nodeElement the node element.
     */
    @FXThread
    public void notifyMoved(@NotNull final ShaderNodeElement<?> nodeElement) {
        notifyResized(nodeElement);
    }

    /**
     * Notify about resized node element.
     *
     * @param nodeElement the node element.
     */
    @FXThread
    public void notifyResized(@NotNull final ShaderNodeElement<?> nodeElement) {

        final Object object = nodeElement.getObject();
        final ShaderNodesChangeConsumer consumer = getChangeConsumer();
        final double width = nodeElement.getPrefWidth();

        final Vector2f location = new Vector2f((float) nodeElement.getLayoutX(), (float) nodeElement.getLayoutY());

        if (object instanceof ShaderNode) {
            consumer.notifyChangeState((ShaderNode) object, location, width);
        } else if (object instanceof ShaderNodeVariable) {
            consumer.notifyChangeState((ShaderNodeVariable) object, location, width);
        } else if (nodeElement instanceof InputGlobalShaderNodeElement) {
            consumer.notifyChangeGlobalNodeState(true, location, width);
        } else if (nodeElement instanceof OutputGlobalShaderNodeElement) {
            consumer.notifyChangeGlobalNodeState(false, location, width);
        }
    }
}
