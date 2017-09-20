package com.ss.editor.shader.nodes.component.shader.nodes;

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
import com.ss.editor.shader.nodes.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.component.shader.nodes.action.add.*;
import com.ss.editor.shader.nodes.component.shader.nodes.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.line.TempLine;
import com.ss.editor.shader.nodes.component.shader.nodes.line.VariableLine;
import com.ss.editor.shader.nodes.component.shader.nodes.main.*;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.util.MaterialDefUtils;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

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
     * All current nodes elements.
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
     * The wrapper of scaled nodes.
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
     * Notify about changed preview material.
     */
    @FXThread
    public void notifyChangedMaterial() {
        getNodeElements().stream()
                .filter(MaterialShaderNodeElement.class::isInstance)
                .map(MaterialShaderNodeElement.class::cast)
                .forEach(MaterialShaderNodeElement::notifyChangedMaterial);
    }

    /**
     * Get all current nodes elements.
     *
     * @return all current nodes elements.
     */
    @FXThread
    private @NotNull Array<ShaderNodeElement<?>> getNodeElements() {
        return nodeElements;
    }

    /**
     * @return the change consumer.
     */
    @FXThread
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

        int skipped = 0;

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
                skipped++;
                continue;
            }

            nodeElement.setLayoutX(location.getX());
            nodeElement.setLayoutY(location.getY());
        }

        if (skipped < 1) {
            return;
        }

        layoutNodes();
    }

    /**
     * Layout nodes.
     */
    @FXThread
    private void layoutNodes() {

        final Array<ShaderNodeElement<?>> nodeElements = getNodeElements();
        final ShaderNodeElement<?> inputElement = nodeElements.stream()
                .filter(InputGlobalShaderNodeElement.class::isInstance)
                .findAny().orElse(null);

        final ShaderNodeElement<?> outputElement = nodeElements.stream()
                .filter(OutputGlobalShaderNodeElement.class::isInstance)
                .findAny().orElse(null);

        Predicate<ShaderNodeElement<?>> isInput = AttributeShaderNodeElement.class::isInstance;
        isInput = isInput.or(MaterialShaderNodeElement.class::isInstance)
                .or(WorldShaderNodeElement.class::isInstance);

        final List<ShaderNodeElement<?>> inputNodes = nodeElements.stream()
                .filter(isInput)
                .sorted((first, second) -> StringUtils.compare(first.getClass().getName(), second.getClass().getName()))
                .collect(toList());

        final List<ShaderNodeElement<?>> vertexNodes = nodeElements.stream()
                .filter(VertexShaderNodeElement.class::isInstance)
                .collect(toList());

        final List<ShaderNodeElement<?>> fragmentNodes = nodeElements.stream()
                .filter(FragmentShaderNodeElement.class::isInstance)
                .collect(toList());

        float inputElementStartX = 10F;
        float inputElementStartY = 10F;
        float inputElementEndY = inputElementStartY;

        if (inputElement != null) {
            inputElement.autosize();
            inputElement.setLayoutX(inputElementStartX);
            inputElement.setLayoutY(inputElementStartY);

            notifyMoved(inputElement);
            notifyResized(inputElement);

            inputElementEndY = (float) (inputElementStartY + inputElement.getHeight() + 30F);
        }

        float inputNodeStartY = inputElementEndY;
        float maxInputParameterWidth = 0;

        for (final ShaderNodeElement<?> inNode : inputNodes) {

            inNode.autosize();
            inNode.setLayoutX(inputElementStartX);
            inNode.setLayoutY(inputNodeStartY);

            notifyMoved(inNode);
            notifyResized(inNode);

            inputNodeStartY += inNode.getHeight() + 30F;
            maxInputParameterWidth = (float) Math.max(maxInputParameterWidth, inNode.getWidth() + 80D);
        }

        float vertexNodeStartX = inputElementStartX + maxInputParameterWidth;
        float vertexNodeStartY = inputElementEndY - 10F;
        float maxVertexNodeWidth = 0F;
        float maxVertexHeight = 0F;

        for (final ShaderNodeElement<?> vertexNode : vertexNodes) {

            vertexNode.autosize();
            vertexNode.setLayoutX(vertexNodeStartX);
            vertexNode.setLayoutY(vertexNodeStartY);

            notifyMoved(vertexNode);
            notifyResized(vertexNode);

            vertexNodeStartX += vertexNode.getWidth() + 50F;
            maxVertexNodeWidth = (float) Math.max(maxVertexNodeWidth, vertexNode.getWidth() + 10D);
            maxVertexHeight = (float) Math.max(maxVertexHeight, vertexNode.getHeight() + 10D);
        }

        float fragmentNodeStartX = inputElementStartX + maxInputParameterWidth + 40F;
        float fragmentNodeStartY = inputElementEndY + 70F + maxVertexHeight;
        float maxFragmentNodeWidth = 0F;
        float maxFragmentHeight = 0F;

        for (final ShaderNodeElement<?> fragmentNode : fragmentNodes) {

            fragmentNode.autosize();
            fragmentNode.setLayoutX(fragmentNodeStartX);
            fragmentNode.setLayoutY(fragmentNodeStartY);

            notifyMoved(fragmentNode);
            notifyResized(fragmentNode);

            fragmentNodeStartX += fragmentNode.getWidth() + 50F;
            maxFragmentNodeWidth = (float) Math.max(maxFragmentNodeWidth, fragmentNode.getWidth() + 10D);
            maxFragmentHeight = (float) Math.max(maxFragmentHeight, fragmentNode.getHeight() + 10D);
        }

        float outputStartX = Math.max(fragmentNodeStartX, vertexNodeStartX);
        outputStartX += 10D;

        float outputStartY = vertexNodeStartY + maxVertexHeight;

        if (outputElement != null) {
            outputElement.autosize();
            outputElement.setLayoutX(outputStartX);
            outputElement.setLayoutY(outputStartY);

            notifyMoved(outputElement);
            notifyResized(outputElement);
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
    @FXThread
    private @Nullable TempLine getTempLine() {
        return tempLine;
    }

    /**
     * Set the temp line to show a process of binding variables.
     *
     * @param tempLine the temp line to show a process of binding variables.
     */
    @FXThread
    private void setTempLine(@Nullable final TempLine tempLine) {
        this.tempLine = tempLine;
    }

    /**
     * Sets to show the process of attaching a variable.
     *
     * @param sourceSocket the source socket.
     */
    @FXThread
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
    @FXThread
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
    @FXThread
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
    @FXThread
    public void handleContextMenuEvent(@NotNull final ContextMenuEvent event) {

        final Object source = event.getSource();

        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }

        final Vector2f location = new Vector2f((float) event.getX(), (float) event.getY());
        final TechniqueDef techniqueDef = getTechniqueDef();
        final MaterialDef materialDef = getChangeConsumer().getMaterialDef();

        final ObservableList<MenuItem> items = contextMenu.getItems();
        items.clear();

        if (source == root) {

            final Menu menu = new Menu("Add");
            menu.getItems().addAll(new AddMaterialParamShaderNodeAction(this, materialDef, location),
                    new AddMaterialTextureShaderNodeAction(this, materialDef, location),
                    new AddWorldParamShaderNodeAction(this, techniqueDef, location),
                    new AddAttributeShaderNodeAction(this, techniqueDef, location),
                    new AddNodeShaderNodeAction(this, techniqueDef, location));

            items.addAll(menu);

        } else if (source instanceof ShaderNodeElement) {

            final ShaderNodeElement<?> nodeElement = (ShaderNodeElement<?>) source;
            final ShaderNodeAction<?> deleteAction = nodeElement.getDeleteAction();

            if(deleteAction != null) {
                items.add(deleteAction);
            }

        } else if (source instanceof VariableLine) {

            final ShaderNodeParameter parameter = ((VariableLine) source).getInParameter();
            final ShaderNodeElement<?> nodeElement = parameter.getNodeElement();
            final ShaderNodeAction<?> detachAction = nodeElement.getDetachAction((VariableLine) source);

            if (detachAction != null) {
                items.add(detachAction);
            }
        }

        contextMenu.show(root, event.getScreenX(), event.getScreenY());
    }

    /**
     * Update the process of attaching a variable.
     *
     * @param sceneX the sceneX.
     * @param sceneY the sceneY.
     */
    @FXThread
    public void updateAttaching(final double sceneX, final double sceneY) {

        final TempLine tempLine = getTempLine();
        if (tempLine == null) return;

        final Point2D localCoords = root.sceneToLocal(sceneX, sceneY);
        tempLine.updateEnd(localCoords.getX(), localCoords.getY());
    }

    /**
     * Handle finish the process of attaching.
     */
    @FXThread
    public void finishAttaching() {

        final TempLine tempLine = getTempLine();
        if (tempLine == null) return;

        FXUtils.removeFromParent(tempLine, root);
        setTempLine(null);
    }

    /**
     * Request to select the nodes.
     *
     * @param requester the nodes to select.
     */
    @FXThread
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
    @FXThread
    private @NotNull Pane getRoot() {
        return root;
    }

    /**
     * Show the technique.
     *
     * @param techniqueDef the technique.
     */
    @FXThread
    public void show(@NotNull final TechniqueDef techniqueDef) {
        if (techniqueDef.equals(this.techniqueDef)) return;

        this.techniqueDef = techniqueDef;

        final ShaderGenerationInfo shaderGenerationInfo = techniqueDef.getShaderGenerationInfo();
        final Pane root = getRoot();
        root.getChildren().clear();

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
     * Find a nodes element by the variable.
     *
     * @param variable the variable.
     * @return the nodes element or null.
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
     * Find a nodes element by the object.
     *
     * @param object the object.
     * @return the nodes element or null.
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
     * Try to find shader nodes parameter for the variable.
     *
     * @param variable          the variable.
     * @param fromOutputMapping true if the variable is from output mapping.
     * @param input             true if the variable is input variable.
     * @return the parameter or null.
     */
    @FXThread
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
     * Add the new material parameter.
     *
     * @param matParam the new material parameter.
     * @param location the location.
     */
    @FXThread
    public void addMatParam(@NotNull final MatParam matParam, @NotNull final Vector2f location) {
        addNodeElement(MaterialShaderNodeElement.toVariable(matParam), location);
    }

    /**
     * Add the new world parameter.
     *
     * @param binding  the binding.
     * @param location the location.
     */
    @FXThread
    public void addWorldParam(@NotNull final UniformBinding binding, @NotNull final Vector2f location) {
        addNodeElement(WorldShaderNodeElement.toVariable(binding), location);
    }

    /**
     * Add the new shader nodes.
     *
     * @param shaderNode the new shader nodes.
     * @param location   the location.
     */
    @FXThread
    public void addShaderNode(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location) {

        final ShaderNodeDefinition definition = shaderNode.getDefinition();
        final MainShaderNodeElement nodeElement;

        if (definition.getType() == Shader.ShaderType.Vertex) {
            nodeElement = new VertexShaderNodeElement(this, shaderNode);
        } else if (definition.getType() == Shader.ShaderType.Fragment) {
            nodeElement = new FragmentShaderNodeElement(this, shaderNode);
        } else {
            throw new RuntimeException("unknown shader nodes type " + shaderNode);
        }

        addNodeElement(nodeElement, location);
    }

    /**
     * Add the new nodes element.
     *
     * @param variable the new shader nodes variable.
     * @param location the location.
     */
    @FXThread
    public void addNodeElement(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {

        final ShaderNodeElement<?> nodeElement = createNodeElement(variable);
        if (nodeElement == null) {
            return;
        }

        addNodeElement(nodeElement, location);
    }

    /**
     * Add the new shader nodes element.
     *
     * @param nodeElement the new nodes element.
     * @param location    the location.
     */
    @FXThread
    private void addNodeElement(@NotNull final ShaderNodeElement<?> nodeElement, @NotNull final Vector2f location) {

        final ObservableList<Node> children = root.getChildren();
        children.add(nodeElement);

        final Array<ShaderNodeElement<?>> nodeElements = getNodeElements();
        nodeElements.add(nodeElement);

        refreshLines();

        EXECUTOR_MANAGER.schedule(() -> {
            EXECUTOR_MANAGER.addFXTask(() -> {
                nodeElement.setLayoutX(location.getX());
                nodeElement.setLayoutY(location.getY());
            });
        }, 50);
    }

    /**
     * Remove the material parameter.
     *
     * @param matParam the material parameter.
     */
    @FXThread
    public void removeMatParam(@NotNull final MatParam matParam) {
        removeNodeElement(MaterialShaderNodeElement.toVariable(matParam));
    }

    /**
     * Remove the world parameter.
     *
     * @param binding the world parameter.
     */
    @FXThread
    public void removeWorldParam(@NotNull final UniformBinding binding) {
        removeNodeElement(WorldShaderNodeElement.toVariable(binding));
    }

    /**
     * Remove the shader nodes.
     *
     * @param shaderNode the shader nodes.
     */
    @FXThread
    public void removeShaderNode(@NotNull final ShaderNode shaderNode) {
        final ShaderNodeElement<?> nodeElement = findNodeElementByObject(shaderNode);
        if (nodeElement == null) return;
        removeNodeElement(nodeElement);
    }

    /**
     * Remove the element of the variable.
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

    /**
     * Remove the nodes element.
     *
     * @param nodeElement the nodes element.
     */
    @FXThread
    private void removeNodeElement(@NotNull final ShaderNodeElement<?> nodeElement) {
        root.getChildren().remove(nodeElement);
        getNodeElements().slowRemove(nodeElement);
        refreshLines();
    }

    /**
     * Create a nodes element for the variable.
     *
     * @param variable the variable.
     * @return the nodes element.
     */
    @FXThread
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
     * Get the current technique definition.
     *
     * @return the current technique definition.
     */
    @FXThread
    public @NotNull TechniqueDef getTechniqueDef() {
        return notNull(techniqueDef);
    }

    /**
     * Get the current material definition.
     *
     * @return the current material definition.
     */
    @FXThread
    public @NotNull MaterialDef getMaterialDef() {
        return getChangeConsumer().getMaterialDef();
    }

    /**
     * Refresh all lines.
     */
    @FXThread
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
    @FXThread
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
    @FXThread
    private void updateScale() {
        root.setScaleX(scaleValue);
        root.setScaleY(scaleValue);
    }

    /**
     * Handle zoom.
     *
     * @param event the scroll event.
     */
    @FXThread
    private void handleScrollEvent(@NotNull final ScrollEvent event) {

        final double zoomFactor = event.getDeltaY() * ZOOM_INTENSITY;
        final double newScale = scaleValue + zoomFactor;

        final Region content = (Region) getContent();
        final Point2D positionInRoot = root.sceneToLocal(event.getSceneX(), event.getSceneY());
        final Point2D positionInContent = content.sceneToLocal(root.localToScene(positionInRoot));

        scaleValue = Math.min(Math.max(newScale, 0.2F), 1F);
        updateScale();
        requestLayout();
        layout();

        final Point2D newPositionInContent = content.sceneToLocal(root.localToScene(positionInRoot));
        final Point2D diff = newPositionInContent.subtract(positionInContent);

        final Bounds viewport = getViewportBounds();
        final Bounds contentBounds = content.getLayoutBounds();

        final double viewScaleX = viewport.getWidth() / contentBounds.getWidth();
        final double viewScaleY = viewport.getHeight() / contentBounds.getHeight();
        final double viewX = diff.getX() * viewScaleX;
        final double viewY = diff.getY() * viewScaleY;
        final double newHValue = viewX / viewport.getWidth();
        final double newYValue = viewY / viewport.getHeight();

        setHvalue(getHvalue() + newHValue);
        setVvalue(getVvalue() + newYValue);
        event.consume();
    }

    /**
     * Notify about moved nodes element.
     *
     * @param nodeElement the nodes element.
     */
    @FXThread
    public void notifyMoved(@NotNull final ShaderNodeElement<?> nodeElement) {
        notifyResized(nodeElement);
    }

    /**
     * Notify about resized nodes element.
     *
     * @param nodeElement the nodes element.
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
