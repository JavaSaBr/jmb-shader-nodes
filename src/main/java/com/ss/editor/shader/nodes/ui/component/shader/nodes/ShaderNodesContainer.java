package com.ss.editor.shader.nodes.ui.component.shader.nodes;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODES_ROOT;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.hasInMappingByRightVar;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.hasOutMappingByLeftVar;
import static com.ss.rlib.util.ObjectUtils.notNull;
import static java.util.stream.Collectors.toList;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.*;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.add.*;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.line.TempLine;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.line.VariableLine;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.*;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.editor.shader.nodes.util.MaterialDefUtils;
import com.ss.editor.shader.nodes.util.ShaderNodeUtils;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        var centered = new VBox(zoomNode);
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
    @FxThread
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
    @FxThread
    private @NotNull Array<ShaderNodeElement<?>> getNodeElements() {
        return nodeElements;
    }

    /**
     * @return the change consumer.
     */
    @FxThread
    public @NotNull ShaderNodesChangeConsumer getChangeConsumer() {
        return changeConsumer;
    }

    /**
     * Reset all layouts.
     */
    @FxThread
    private void invalidateSizes() {

        var consumer = getChangeConsumer();
        var nodeElements = getNodeElements();

        for (var nodeElement : nodeElements) {
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

        EXECUTOR_MANAGER.addFxTask(this::invalidateLayout);
    }

    /**
     * Update all layouts.
     */
    @FxThread
    private void invalidateLayout() {

        var consumer = getChangeConsumer();
        var nodeElements = getNodeElements();
        var skipped = 0;

        for (var nodeElement : nodeElements) {
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
    @FxThread
    private void layoutNodes() {

        var nodeElements = getNodeElements();
        var inputElement = nodeElements.stream()
                .filter(InputGlobalShaderNodeElement.class::isInstance)
                .findAny().orElse(null);

        var outputElement = nodeElements.stream()
                .filter(OutputGlobalShaderNodeElement.class::isInstance)
                .findAny().orElse(null);

        Predicate<ShaderNodeElement<?>> isInput = AttributeShaderNodeElement.class::isInstance;
        isInput = isInput.or(MaterialShaderNodeElement.class::isInstance)
                .or(WorldShaderNodeElement.class::isInstance);

        var inputNodes = nodeElements.stream()
                .filter(isInput)
                .sorted((first, second) -> StringUtils.compare(first.getClass().getName(), second.getClass().getName()))
                .collect(toList());

        var vertexNodes = nodeElements.stream()
                .filter(VertexShaderNodeElement.class::isInstance)
                .collect(toList());

        var fragmentNodes = nodeElements.stream()
                .filter(FragmentShaderNodeElement.class::isInstance)
                .collect(toList());

        var inputElementStartX = 10F;
        var inputElementStartY = 10F;
        var inputElementEndY = inputElementStartY;

        if (inputElement != null) {
            inputElement.autosize();
            inputElement.setLayoutX(inputElementStartX);
            inputElement.setLayoutY(inputElementStartY);

            notifyMoved(inputElement);
            notifyResized(inputElement);

            inputElementEndY = (float) (inputElementStartY + inputElement.getHeight() + 30F);
        }

        var inputNodeStartY = inputElementEndY;
        var maxInputParameterWidth = 0F;

        for (var inNode : inputNodes) {

            inNode.autosize();
            inNode.setLayoutX(inputElementStartX);
            inNode.setLayoutY(inputNodeStartY);

            notifyMoved(inNode);
            notifyResized(inNode);

            inputNodeStartY += inNode.getHeight() + 30F;
            maxInputParameterWidth = (float) Math.max(maxInputParameterWidth, inNode.getWidth() + 80D);
        }

        var vertexNodeStartX = inputElementStartX + maxInputParameterWidth;
        var vertexNodeStartY = inputElementEndY - 10F;
        var maxVertexNodeWidth = 0F;
        var maxVertexHeight = 0F;

        for (var vertexNode : vertexNodes) {

            vertexNode.autosize();
            vertexNode.setLayoutX(vertexNodeStartX);
            vertexNode.setLayoutY(vertexNodeStartY);

            notifyMoved(vertexNode);
            notifyResized(vertexNode);

            vertexNodeStartX += vertexNode.getWidth() + 50F;
            maxVertexNodeWidth = (float) Math.max(maxVertexNodeWidth, vertexNode.getWidth() + 10D);
            maxVertexHeight = (float) Math.max(maxVertexHeight, vertexNode.getHeight() + 10D);
        }

        var fragmentNodeStartX = inputElementStartX + maxInputParameterWidth + 40F;
        var fragmentNodeStartY = inputElementEndY + 70F + maxVertexHeight;
        var maxFragmentNodeWidth = 0F;
        var maxFragmentHeight = 0F;

        for (var fragmentNode : fragmentNodes) {

            fragmentNode.autosize();
            fragmentNode.setLayoutX(fragmentNodeStartX);
            fragmentNode.setLayoutY(fragmentNodeStartY);

            notifyMoved(fragmentNode);
            notifyResized(fragmentNode);

            fragmentNodeStartX += fragmentNode.getWidth() + 50F;
            maxFragmentNodeWidth = (float) Math.max(maxFragmentNodeWidth, fragmentNode.getWidth() + 10D);
            maxFragmentHeight = (float) Math.max(maxFragmentHeight, fragmentNode.getHeight() + 10D);
        }

        var outputStartX = Math.max(fragmentNodeStartX, vertexNodeStartX);
        outputStartX += 10D;

        var outputStartY = vertexNodeStartY + maxVertexHeight;

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
    @FxThread
    private void resetLayout() {
        getNodeElements().forEach(ShaderNodeElement::resetLayout);
    }

    /**
     * Get the temp line to show a process of binding variables.
     *
     * @return the temp line to show a process of binding variables.
     */
    @FxThread
    private @Nullable TempLine getTempLine() {
        return tempLine;
    }

    /**
     * Set the temp line to show a process of binding variables.
     *
     * @param tempLine the temp line to show a process of binding variables.
     */
    @FxThread
    private void setTempLine(@Nullable final TempLine tempLine) {
        this.tempLine = tempLine;
    }

    /**
     * Sets to show the process of attaching a variable.
     *
     * @param sourceSocket the source socket.
     */
    @FxThread
    public void startAttaching(@NotNull final SocketElement sourceSocket) {

        var tempLine = getTempLine();
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
    @FxThread
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
    @FxThread
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
    @FxThread
    public void handleContextMenuEvent(@NotNull final ContextMenuEvent event) {

        var source = event.getSource();

        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }

        var location = new Vector2f((float) event.getX(), (float) event.getY());
        var techniqueDef = getTechniqueDef();
        var materialDef = getChangeConsumer().getMaterialDef();

        var items = contextMenu.getItems();
        items.clear();

        if (source == root) {

            //FIXME localization
            var menu = new Menu("Add");
            menu.getItems().addAll(new AddMaterialParamShaderNodeAction(this, materialDef, location),
                    new AddMaterialTextureShaderNodeAction(this, materialDef, location),
                    new AddWorldParamShaderNodeAction(this, techniqueDef, location),
                    new AddAttributeShaderNodeAction(this, techniqueDef, location),
                    new AddNodeShaderNodeAction(this, techniqueDef, location));

            items.addAll(menu);

        } else if (source instanceof ShaderNodeElement) {

            var nodeElement = (ShaderNodeElement<?>) source;
            var deleteAction = nodeElement.getDeleteAction();

            if(deleteAction != null) {
                items.add(deleteAction);
            }

        } else if (source instanceof VariableLine) {

            var parameter = ((VariableLine) source).getInParameter();
            var nodeElement = parameter.getNodeElement();
            var detachAction = nodeElement.getDetachAction((VariableLine) source);

            if (detachAction != null) {
                items.add(detachAction);
            }
        }

        contextMenu.show(root, event.getScreenX(), event.getScreenY());

        event.consume();
    }

    /**
     * Update the process of attaching a variable.
     *
     * @param sceneX the sceneX.
     * @param sceneY the sceneY.
     */
    @FxThread
    public void updateAttaching(final double sceneX, final double sceneY) {

        var tempLine = getTempLine();
        if (tempLine == null) {
            return;
        }

        var localCoords = root.sceneToLocal(sceneX, sceneY);
        tempLine.updateEnd(localCoords.getX(), localCoords.getY());
    }

    /**
     * Handle finish the process of attaching.
     */
    @FxThread
    public void finishAttaching() {

        var tempLine = getTempLine();
        if (tempLine == null) {
            return;
        }

        FXUtils.removeFromParent(tempLine, root);
        setTempLine(null);
    }

    /**
     * Request to select the nodes.
     *
     * @param requester the nodes to select.
     */
    @FxThread
    public void requestSelect(@NotNull final ShaderNodeElement<?> requester) {

        root.getChildren().stream()
            .filter(node -> node != requester)
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
    @FxThread
    private @NotNull Pane getRoot() {
        return root;
    }

    /**
     * Show the technique.
     *
     * @param techniqueDef the technique.
     */
    @FxThread
    public void show(@NotNull final TechniqueDef techniqueDef) {

        if (techniqueDef.equals(this.techniqueDef)) {
            return;
        }

        this.techniqueDef = techniqueDef;

        var shaderGenerationInfo = techniqueDef.getShaderGenerationInfo();
        var root = getRoot();
        root.getChildren().clear();

        var nodeElements = getNodeElements();
        nodeElements.clear();

        var consumer = getChangeConsumer();
        var materialDef = consumer.getMaterialDef();

        var matParams = MaterialDefUtils.getMatParams(materialDef);
        var worldBindings = techniqueDef.getWorldBindings();

        final List<ShaderNodeVariable> uniforms = new ArrayList<>();

        for (var matParam : matParams.values()) {
            uniforms.add(MaterialShaderNodeElement.toVariable(matParam));
        }

        for (var worldBinding : worldBindings) {
            uniforms.add(WorldShaderNodeElement.toVariable(worldBinding));
        }

        uniforms.addAll(shaderGenerationInfo.getAttributes());

        nodeElements.add(new InputGlobalShaderNodeElement(this, shaderGenerationInfo));
        nodeElements.add(new OutputGlobalShaderNodeElement(this, shaderGenerationInfo));

        for (var variable : uniforms) {
            createNodeElement(variable).ifPresent(nodeElements::add);
        }

        var shaderNodes = techniqueDef.getShaderNodes();

        for (var shaderNode : shaderNodes) {
            var definition = shaderNode.getDefinition();
            if (definition.getType() == Shader.ShaderType.Vertex) {
                nodeElements.add(new VertexShaderNodeElement(this, shaderNode));
            } else if (definition.getType() == Shader.ShaderType.Fragment) {
                nodeElements.add(new FragmentShaderNodeElement(this, shaderNode));
            }
        }

        nodeElements.forEach(root.getChildren(),
            (nodeElement, nodes) -> nodes.add(nodeElement));

        refreshLines();

        EXECUTOR_MANAGER.addFxTask(this::invalidateSizes);
    }

    /**
     * Find all shader nodes with left output variables.
     *
     * @param leftVariable the left variable.
     * @return the list of nodes.
     */
    @FxThread
    public @NotNull List<ShaderNode> findWithLeftOutputVar(@NotNull final ShaderNodeVariable leftVariable) {
        return root.getChildren().stream()
                .filter(MainShaderNodeElement.class::isInstance)
                .map(MainShaderNodeElement.class::cast)
                .map(ShaderNodeElement::getObject)
                .filter(shaderNode -> hasOutMappingByLeftVar(shaderNode, leftVariable))
                .collect(toList());
    }

    /**
     * Find a shader node by the name.
     *
     * @param name the name.
     * @return the optional of the shader node.
     */
    @FxThread
    private ShaderNode findShaderNodeByName(@NotNull final String name) {

        if (ShaderNodeUtils.isUserShaderNode(name)) {
            return null;
        }

        return root.getChildren().stream()
                .filter(MainShaderNodeElement.class::isInstance)
                .map(MainShaderNodeElement.class::cast)
                .map(ShaderNodeElement::getObject)
                .filter(shaderNode -> shaderNode.getName().equals(name))
                .findAny().orElse(null);
    }

    /**
     * Find a variable line for the shader node input parameter.
     *
     * @param parameter the input parameter.
     * @return a variable line for the shader node input parameter.
     */
    public @NotNull Optional<VariableLine> findLineByInParameter(@NotNull final InputShaderNodeParameter parameter) {
        return root.getChildren().stream()
            .filter(VariableLine.class::isInstance)
            .map(VariableLine.class::cast)
            .filter(line -> line.getInParameter() == parameter)
            .findAny();
    }

    /**
     * Find all shader nodes which are used from the shader node.
     *
     * @param shaderNode the shader node.
     * @return the list of used shader nodes.
     */
    @FxThread
    public @NotNull List<ShaderNode> findUsedFrom(@NotNull final ShaderNode shaderNode) {
        return findUsedFrom(new ArrayList<>(), shaderNode);
    }

    @FxThread
    private @NotNull List<ShaderNode> findUsedFrom(@NotNull final List<ShaderNode> result,
                                                   @NotNull final ShaderNode shaderNode) {

        for (var mapping : shaderNode.getInputMapping()) {

            var rightVariable = mapping.getRightVariable();
            var usedNode = findShaderNodeByName(rightVariable.getNameSpace());

            if (usedNode == null || result.contains(usedNode)) {
                continue;
            }

            result.add(usedNode);
            findUsedFrom(result, usedNode);
        }

        return result;
    }

    /**
     * Find all shader nodes with right input variables.
     *
     * @param rightVariable the right variable.
     * @param type          the type.
     * @return the list of nodes.
     */
    @FxThread
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
     * @return the node element.
     */
    @FxThread
    private @NotNull Optional<? extends ShaderNodeElement<?>> findNodeElementByVariable(@NotNull final ShaderNodeVariable variable) {
        return root.getChildren().stream()
                    .filter(ShaderNodeElement.class::isInstance)
                    .map(sne -> (ShaderNodeElement<?>) sne)
                    .filter(element -> element.getObject().equals(variable))
                    .findAny();
    }

    /**
     * Find a nodes element by the object.
     *
     * @param object the object.
     * @return the node element.
     */
    @FxThread
    private @NotNull Optional<? extends ShaderNodeElement<?>> findNodeElementByObject(@NotNull final Object object) {
        return root.getChildren().stream()
                .filter(ShaderNodeElement.class::isInstance)
                .map(sne -> (ShaderNodeElement<?>) sne)
                .filter(element -> element.getObject().equals(object))
                .findAny();
    }

    /**
     * Try to find shader nodes parameter for the variable.
     *
     * @param variable          the variable.
     * @param fromOutputMapping true if the variable is from output mapping.
     * @param input             true if the variable is input variable.
     * @return the parameter or null.
     */
    @FxThread
    private @Nullable ShaderNodeParameter findByVariable(@NotNull final ShaderNodeVariable variable,
                                                         final boolean fromOutputMapping,
                                                         final boolean input) {
        return root.getChildren().stream()
                .filter(ShaderNodeElement.class::isInstance)
                .map(ShaderNodeElement.class::cast)
                .map(sne -> sne.parameterFor(variable, fromOutputMapping, input))
                .filter(Objects::nonNull)
                .findAny().orElse(null);
    }

    /**
     * Add the new material parameter.
     *
     * @param matParam the new material parameter.
     * @param location the location.
     */
    @FxThread
    public void addMatParam(@NotNull final MatParam matParam, @NotNull final Vector2f location) {
        addNodeElement(MaterialShaderNodeElement.toVariable(matParam), location);
    }

    /**
     * Add the new world parameter.
     *
     * @param binding  the binding.
     * @param location the location.
     */
    @FxThread
    public void addWorldParam(@NotNull final UniformBinding binding, @NotNull final Vector2f location) {
        addNodeElement(WorldShaderNodeElement.toVariable(binding), location);
    }

    /**
     * Add the new shader nodes.
     *
     * @param shaderNode the new shader nodes.
     * @param location   the location.
     */
    @FxThread
    public void addShaderNode(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location) {

        var definition = shaderNode.getDefinition();

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
    @FxThread
    public void addNodeElement(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {
        createNodeElement(variable).ifPresent(element -> addNodeElement(element, location));
    }

    /**
     * Add the new shader nodes element.
     *
     * @param nodeElement the new nodes element.
     * @param location    the location.
     */
    @FxThread
    private void addNodeElement(@NotNull final ShaderNodeElement<?> nodeElement, @NotNull final Vector2f location) {

        var children = root.getChildren();
        children.add(nodeElement);

        var nodeElements = getNodeElements();
        nodeElements.add(nodeElement);

        refreshLines();

        EXECUTOR_MANAGER.schedule(() -> {
            EXECUTOR_MANAGER.addFxTask(() -> {
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
    @FxThread
    public void removeMatParam(@NotNull final MatParam matParam) {
        removeNodeElement(MaterialShaderNodeElement.toVariable(matParam));
    }

    /**
     * Remove the world parameter.
     *
     * @param binding the world parameter.
     */
    @FxThread
    public void removeWorldParam(@NotNull final UniformBinding binding) {
        removeNodeElement(WorldShaderNodeElement.toVariable(binding));
    }

    /**
     * Remove the shader node.
     *
     * @param shaderNode the shader node.
     */
    @FxThread
    public void removeShaderNode(@NotNull final ShaderNode shaderNode) {
        findNodeElementByObject(shaderNode)
            .ifPresent(this::removeNodeElement);
    }

    /**
     * Refresh the shader node.
     *
     * @param shaderNode the shader node.
     */
    @FxThread
    public void refreshShaderNode(@NotNull final ShaderNode shaderNode) {
        findNodeElementByObject(shaderNode)
            .ifPresent(ShaderNodeElement::refresh);
    }

    /**
     * Remove the element of the variable.
     *
     * @param variable the variable.
     */
    @FxThread
    public void removeNodeElement(@NotNull final ShaderNodeVariable variable) {
        findNodeElementByVariable(variable).ifPresent(this::removeNodeElement);
    }

    /**
     * Remove the nodes element.
     *
     * @param nodeElement the nodes element.
     */
    @FxThread
    private void removeNodeElement(@NotNull final ShaderNodeElement<?> nodeElement) {
        root.getChildren().remove(nodeElement);
        getNodeElements().slowRemove(nodeElement);
        refreshLines();
    }

    /**
     * Create a nodes element for the variable.
     *
     * @param variable the variable.
     * @return the optional of shader node element.
     */
    @FxThread
    private @NotNull Optional<ShaderNodeElement<?>> createNodeElement(@NotNull final ShaderNodeVariable variable) {
        return ShaderNodeUtils.createNodeElement(this, variable);
    }

    /**
     * Get the current technique definition.
     *
     * @return the current technique definition.
     */
    @FxThread
    public @NotNull TechniqueDef getTechniqueDef() {
        return notNull(techniqueDef);
    }

    /**
     * Get the current material definition.
     *
     * @return the current material definition.
     */
    @FxThread
    public @NotNull MaterialDef getMaterialDef() {
        return getChangeConsumer().getMaterialDef();
    }

    /**
     * Refresh all lines.
     */
    @FxThread
    public void refreshLines() {

        var children = root.getChildren();
        var lines = children.stream()
                .filter(VariableLine.class::isInstance)
                .collect(toList());

        children.removeAll(lines);

        var shaderNodes = getTechniqueDef().getShaderNodes();

        for (var shaderNode : shaderNodes) {

            var inputMapping = shaderNode.getInputMapping();
            var outputMapping = shaderNode.getOutputMapping();

            buildLines(children, inputMapping, false);
            buildLines(children, outputMapping, true);
        }

        // we need to copy lines to a separated list,
        // because the method Node.toBack modifies the children's list.
        var toBack = children.stream()
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
    @FxThread
    private void buildLines(@NotNull final ObservableList<Node> children,
                            @NotNull final List<VariableMapping> mappings,
                            final boolean fromOutputMapping) {

        for (var variableMapping : mappings) {

            var leftVariable = variableMapping.getLeftVariable();
            var rightVariable = variableMapping.getRightVariable();

            if (rightVariable == null) {
                continue;
            }

            var leftParameter = findByVariable(leftVariable, fromOutputMapping, true);
            var rightParameter = findByVariable(rightVariable, fromOutputMapping, false);

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
    @FxThread
    private void updateScale() {
        root.setScaleX(scaleValue);
        root.setScaleY(scaleValue);
    }

    /**
     * Handle zoom.
     *
     * @param event the scroll event.
     */
    @FxThread
    private void handleScrollEvent(@NotNull final ScrollEvent event) {

        var zoomFactor = event.getDeltaY() * ZOOM_INTENSITY;
        var newScale = scaleValue + zoomFactor;

        var content = (Region) getContent();
        var positionInRoot = root.sceneToLocal(event.getSceneX(), event.getSceneY());
        var positionInContent = content.sceneToLocal(root.localToScene(positionInRoot));

        scaleValue = Math.min(Math.max(newScale, 0.2F), 1F);
        updateScale();
        requestLayout();
        layout();

        var newPositionInContent = content.sceneToLocal(root.localToScene(positionInRoot));
        var diff = newPositionInContent.subtract(positionInContent);

        var viewport = getViewportBounds();
        var contentBounds = content.getLayoutBounds();

        var viewScaleX = viewport.getWidth() / contentBounds.getWidth();
        var viewScaleY = viewport.getHeight() / contentBounds.getHeight();
        var viewX = diff.getX() * viewScaleX;
        var viewY = diff.getY() * viewScaleY;
        var newHValue = viewX / viewport.getWidth();
        var newYValue = viewY / viewport.getHeight();

        setHvalue(getHvalue() + newHValue);
        setVvalue(getVvalue() + newYValue);
        event.consume();
    }

    /**
     * Notify about moved nodes element.
     *
     * @param nodeElement the nodes element.
     */
    @FxThread
    public void notifyMoved(@NotNull final ShaderNodeElement<?> nodeElement) {
        notifyResized(nodeElement);
    }

    /**
     * Notify about resized nodes element.
     *
     * @param nodeElement the nodes element.
     */
    @FxThread
    public void notifyResized(@NotNull final ShaderNodeElement<?> nodeElement) {

        var object = nodeElement.getObject();
        var consumer = getChangeConsumer();
        var width = nodeElement.getPrefWidth();

        var location = new Vector2f((float) nodeElement.getLayoutX(), (float) nodeElement.getLayoutY());

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
