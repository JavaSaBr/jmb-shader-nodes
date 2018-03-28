package com.ss.editor.shader.nodes.ui.component.shader.nodes;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE;
import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_HEADER;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.*;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.remove.RemoveRelationShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.line.VariableLine;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.editor.shader.nodes.util.ShaderNodeUtils;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The base implementation of shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodeElement<T> extends VBox {

    /**
     * The margin around the control that a user can click in to start resizing
     * the region.
     */
    private static final int RESIZE_MARGIN = 5;

    @NotNull
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /**
     * The selected state.
     */
    @NotNull
    private final BooleanProperty selected = new BooleanPropertyBase(false) {

        @Override
        public void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
        }

        @Override
        public Object getBean() {
            return ShaderNodeElement.this;
        }

        @Override
        public String getName() {
            return "selected";
        }
    };

    /**
     * The container of this nodes.
     */
    @NotNull
    private final ShaderNodesContainer container;

    /**
     * The parameters container.
     */
    @NotNull
    private final VBox parametersContainer;

    /**
     * The shader object.
     */
    @NotNull
    private final T object;

    // nodes position
    private double x;
    private double y;

    // mouse position
    private double mouseX;
    private double mouseY;

    /**
     * The dragging state.
     */
    private boolean dragging;

    /**
     * The resizing state.
     */
    private boolean resizing;

    public ShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final T object) {
        this.container = container;
        this.object = object;
        this.parametersContainer = new VBox();
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseMoved(this::handleMouseMoved);
        setOnMouseReleased(this::handleMouseReleased);
        setOnContextMenuRequested(this::handleContextMenuRequested);
        createContent();
        setPrefWidth(200);
        createTooltip().ifPresent(tooltip -> Tooltip.install(this, tooltip));
        FXUtils.addClassTo(this, SHADER_NODE);
    }

    /**
     * Get the container of nodes parameters.
     *
     * @return the container of nodes parameters.
     */
    @FxThread
    protected @NotNull VBox getParametersContainer() {
        return parametersContainer;
    }

    /**
     * Create a tooltip of this node element.
     *
     * @return the tooltip.
     */
    @FxThread
    protected @NotNull Optional<Tooltip> createTooltip() {
        return Optional.empty();
    }

    /**
     * Handle the context menu requested events.
     *
     * @param event the context menu requested event.
     */
    @FxThread
    private void handleContextMenuRequested(@NotNull final ContextMenuEvent event) {
        getContainer().handleContextMenuEvent(event);
    }

    /**
     * Check the availability to attach the output parameter to the input parameter.
     *
     * @param inputParameter  the input parameter.
     * @param outputParameter the output parameter.
     * @return true of we can attach.
     */
    @FxThread
    public boolean canAttach(@NotNull final InputShaderNodeParameter inputParameter,
                             @NotNull final OutputShaderNodeParameter outputParameter) {

        if (inputParameter.getNodeElement() == outputParameter.getNodeElement()) {
            return false;
        }

        var inVar = inputParameter.getVariable();
        var outVar = outputParameter.getVariable();
        var inType = inVar.getType();
        var outType = outVar.getType();

        return isAccessibleType(inType, outType) || !StringUtils.isEmpty(calculateRightSwizzling(inVar, outVar)) ||
                !StringUtils.isEmpty(calculateLeftSwizzling(inVar, outVar));
    }

    /**
     * Attach the output parameter to the input parameter.
     *
     * @param inputParameter  the input parameter.
     * @param outputParameter the output parameter.
     */
    @FxThread
    public void attach(@NotNull final InputShaderNodeParameter inputParameter,
                       @NotNull final OutputShaderNodeParameter outputParameter) {
    }

    /**
     * Get the container of this nodes.
     *
     * @return the container of this nodes.
     */
    @FxThread
    public @NotNull ShaderNodesContainer getContainer() {
        return container;
    }

    /**
     * Get the shader object.
     *
     * @return the shader object.
     */
    @FxThread
    public @NotNull T getObject() {
        return object;
    }

    /**
     * Try to find parameter of the variable.
     *
     * @param variable          the variable.
     * @param fromOutputMapping true if the variable is from output mapping.
     * @param input             true if the variable is input variable.
     * @return the parameter or null.
     */
    @FxThread
    public @Nullable ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping,
                                                      final boolean input) {
        return parametersContainer.getChildren().stream()
                .filter(ShaderNodeParameter.class::isInstance)
                .map(ShaderNodeParameter.class::cast)
                .filter(node -> input == isInput(node))
                .filter(parameter -> parameter.getVariable().getName().equals(variable.getName()))
                .findAny().orElse(null);
    }

    /**
     * Get the title text of this nodes.
     *
     * @return the title text of this nodes.
     */
    @FxThread
    protected @NotNull String getTitleText() {
        return "Title";
    }

    /**
     * Create UI content of this nodes.
     */
    @FxThread
    protected void createContent() {

        var header = new StackPane();
        var titleLabel = new Label(getTitleText());

        FXUtils.addClassTo(header, SHADER_NODE_HEADER);

        FXUtils.addToPane(titleLabel, header);
        FXUtils.addToPane(header, this);
        FXUtils.addToPane(parametersContainer, this);

        fillParameters(parametersContainer);
    }

    /**
     * Reset layout of this nodes.
     */
    @FxThread
    public void resetLayout() {
        var layoutX = getLayoutX();
        var layoutY = getLayoutY();
        setLayoutX(-1D);
        setLayoutY(-1D);
        setLayoutX(layoutX);
        setLayoutY(layoutY);
    }

    /**
     * Refresh this component.
     */
    @FxThread
    public void refresh() {
        getParametersContainer().getChildren()
            .stream()
            .map(ShaderNodeParameter.class::cast)
            .forEach(ShaderNodeParameter::refresh);
    }

    /**
     * Fill parameters of this nodes.
     *
     * @param container the parameters container.
     */
    @FxThread
    protected void fillParameters(@NotNull final VBox container) {
    }

    /**
     * Handle mouse dragged event.
     *
     * @param event the mouse event.
     */
    @FxThread
    private void handleMouseDragged(@NotNull final MouseEvent event) {

        if (event.getTarget() instanceof SocketElement) {
            return;
        }

        if (isResizing()) {
            var mouseX = event.getX();
            setPrefWidth(getPrefWidth() + (mouseX - x));
            resetLayout();
            x = mouseX;
        } else {

            var parent = getParent();
            var posInParent = parent.sceneToLocal(event.getSceneX(), event.getSceneY());
            var offsetX = posInParent.getX() - mouseX;
            var offsetY = posInParent.getY() - mouseY;

            x = Math.max(x + offsetX, 10D);
            y = Math.max(y + offsetY, 10D);

            setLayoutX(x);
            setLayoutY(y);
            setDragging(true);

            // again set current Mouse x AND y position
            mouseX = posInParent.getX();
            mouseY = posInParent.getY();
        }

        event.consume();
    }

    /**
     * Handle mouse released event.
     *
     * @param event the mouse released event.
     */
    @FxThread
    private void handleMouseReleased(@NotNull final MouseEvent event) {
        if (isResizing()) {
            setResizing(false);
            setCursor(Cursor.DEFAULT);
            resetLayout();
            getContainer().notifyResized(this);
        } else if (isDragging()) {
            setDragging(false);
            getContainer().notifyMoved(this);
        }
    }

    /**
     * Handle mouse moved event.
     *
     * @param event the mouse moved event.
     */
    @FxThread
    private void handleMouseMoved(@NotNull final MouseEvent event) {

        if (event.getTarget() instanceof SocketElement) {
            setCursor(Cursor.DEFAULT);
            return;
        }

        if (isInResizableZone(event) || isResizing()) {
            setCursor(Cursor.W_RESIZE);
        } else {
            setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Set the selected state.
     *
     * @param selected true if this nodes is selected.
     */
    @FxThread
    public void setSelected(final boolean selected) {
        this.selected.setValue(selected);
    }

    /**
     * Handle mouse pressed event.
     *
     * @param event the mouse pressed event.
     */
    @FxThread
    private void handleMousePressed(@NotNull final MouseEvent event) {

        if (event.getTarget() instanceof SocketElement) {
            return;
        }

        if (event.getButton() != MouseButton.MIDDLE) {
            container.requestSelect(this);
        }

        if (isInResizableZone(event)) {
            setResizing(true);
            x = event.getX();
        } else {

            final var parent = getParent();
            final var posInParent = parent.sceneToLocal(event.getSceneX(), event.getSceneY());

            // record the current mouse X and Y position on Node
            mouseX = posInParent.getX();
            mouseY = posInParent.getY();

            x = getLayoutX();
            y = getLayoutY();

            toFront();
        }
    }

    /**
     * Check if we can resize this nodes.
     *
     * @param event the mouse event.
     * @return true if we can start to resize.
     */
    @FxThread
    protected boolean isInResizableZone(@NotNull final MouseEvent event) {
        return event.getX() > (getWidth() - RESIZE_MARGIN);
    }

    /**
     * Return true if this nodes is dragging now.
     *
     * @return true if this nodes is dragging now.
     */
    @FxThread
    protected boolean isDragging() {
        return dragging;
    }

    /**
     * Set true if this nodes is dragging now.
     *
     * @param dragging true if this nodes is dragging now.
     */
    @FxThread
    protected void setDragging(final boolean dragging) {
        this.dragging = dragging;
    }

    /**
     * Return true if this nodes is resizing now.
     *
     * @return true if this nodes is resizing now.
     */
    @FxThread
    protected boolean isResizing() {
        return resizing;
    }

    /**
     * Set true if this nodes is resizing now.
     *
     * @param resizing true if this nodes is resizing now.
     */
    @FxThread
    protected void setResizing(final boolean resizing) {
        this.resizing = resizing;
    }

    /**
     * Get an action to delete this element.
     *
     * @return the action or null.
     */
    @FxThread
    public @Nullable ShaderNodeAction<?> getDeleteAction() {
        return null;
    }

    /**
     * Get an action to detach the relation.
     *
     * @param line the line.
     * @return the action or null.
     */
    @FxThread
    public @Nullable ShaderNodeAction<?> getDetachAction(@NotNull final VariableLine line) {
        return new RemoveRelationShaderNodeAction(getContainer(), line, Vector2f.ZERO);
    }
}
