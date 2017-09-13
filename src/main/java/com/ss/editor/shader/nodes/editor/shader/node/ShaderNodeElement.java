package com.ss.editor.shader.nodes.editor.shader.node;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODE;
import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODE_HEADER;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.calculateRightSwizzling;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.socket.SocketElement;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base implementation of shader node.
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
     * The container of this node.
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

    // node position
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
        setOnMousePressed(this::handleMousePressed);
        setOnMouseDragged(this::handleMouseDragged);
        setOnMouseMoved(this::handleMouseMoved);
        setOnMouseReleased(this::handleMouseReleased);
        this.object = object;
        this.parametersContainer = new VBox();
        createContent();
        setPrefWidth(200);
        FXUtils.addClassTo(this, SHADER_NODE);
    }

    /**
     * Check the availability to attach the output parameter to the input parameter.
     *
     * @param inputParameter  the input parameter.
     * @param outputParameter the output parameter.
     * @return true of we can attach.
     */
    @FXThread
    public boolean canAttach(@NotNull final InputShaderNodeParameter inputParameter,
                             @NotNull final OutputShaderNodeParameter outputParameter) {

        if (inputParameter.getNodeElement() == outputParameter.getNodeElement()) {
            return false;
        }

        final ShaderNodeVariable inVar = inputParameter.getVariable();
        final ShaderNodeVariable outVar = outputParameter.getVariable();

        return StringUtils.equals(inVar.getType(), outVar.getType()) ||
                !StringUtils.isEmpty(calculateRightSwizzling(inVar, outVar));
    }

    /**
     * Attach the output parameter to the input parameter.
     *
     * @param inputParameter  the input parameter.
     * @param outputParameter the output parameter.
     */
    @FXThread
    public void attach(@NotNull final InputShaderNodeParameter inputParameter,
                       @NotNull final OutputShaderNodeParameter outputParameter) {
    }

    /**
     * @return the container of this node.
     */
    @FXThread
    public @NotNull ShaderNodesContainer getContainer() {
        return container;
    }

    /**
     * @return the shader object.
     */
    @FXThread
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
    @FXThread
    public @Nullable ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {
        return parametersContainer.getChildren().stream()
                .filter(ShaderNodeParameter.class::isInstance)
                .filter(node -> input ? node instanceof InputShaderNodeParameter : node instanceof OutputShaderNodeParameter)
                .map(ShaderNodeParameter.class::cast)
                .filter(parameter -> parameter.getVariable().getName().equals(variable.getName()))
                .findAny().orElse(null);
    }

    /**
     * Get the title text of this node.
     *
     * @return the title text of this node.
     */
    @FXThread
    protected @NotNull String getTitleText() {
        return "Title";
    }

    /**
     * Create UI content of this node.
     */
    @FXThread
    protected void createContent() {

        final StackPane header = new StackPane();
        final Label titleLabel = new Label(getTitleText());

        FXUtils.addClassTo(header, SHADER_NODE_HEADER);

        FXUtils.addToPane(titleLabel, header);
        FXUtils.addToPane(header, this);
        FXUtils.addToPane(parametersContainer, this);

        fillParameters(parametersContainer);
    }

    /**
     * Reset layout of this node.
     */
    @FXThread
    public void resetLayout() {
        final double layoutX = getLayoutX();
        final double layoutY = getLayoutY();
        setLayoutX(-1D);
        setLayoutY(-1D);
        setLayoutX(layoutX);
        setLayoutY(layoutY);
    }

    /**
     * Fill parameters of this node.
     *
     * @param container the parameters container.
     */
    @FXThread
    protected void fillParameters(@NotNull final VBox container) {
    }

    /**
     * Handle mouse dragged event.
     *
     * @param event the mouse event.
     */
    @FXThread
    private void handleMouseDragged(@NotNull final MouseEvent event) {

        if (event.getTarget() instanceof SocketElement) {
            return;
        }

        if (isResizing()) {
            final double mouseX = event.getX();
            setPrefWidth(getPrefWidth() + (mouseX - x));
            resetLayout();
            x = mouseX;
        } else {

            final Parent parent = getParent();
            final Point2D parentCoords = parent.sceneToLocal(event.getSceneX(), event.getSceneY());

            double offsetX = parentCoords.getX() - mouseX;
            double offsetY = parentCoords.getY() - mouseY;

            x = Math.max(x + offsetX, 10D);
            y = Math.max(y + offsetY, 10D);

            setLayoutX(x);
            setLayoutY(y);
            setDragging(true);

            // again set current Mouse x AND y position
            mouseX = parentCoords.getX();
            mouseY = parentCoords.getY();
        }

        event.consume();
    }

    /**
     * Handle mouse released event.
     *
     * @param event the mouse released event.
     */
    @FXThread
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
    @FXThread
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
     * @param selected true if this node is selected.
     */
    @FXThread
    public void setSelected(final boolean selected) {
        this.selected.setValue(selected);
    }

    /**
     * Handle mouse pressed event.
     *
     * @param event the mouse pressed event.
     */
    @FXThread
    private void handleMousePressed(@NotNull final MouseEvent event) {

        if (event.getTarget() instanceof SocketElement) {
            return;
        } else if (event.getButton() != MouseButton.MIDDLE) {
            container.requestSelect(this);
        }

        if (isInResizableZone(event)) {
            setResizing(true);
            x = event.getX();
        } else {

            final Parent parent = getParent();
            final Point2D parentCoords = parent.sceneToLocal(event.getSceneX(), event.getSceneY());

            // record the current mouse X and Y position on Node
            mouseX = parentCoords.getX();
            mouseY = parentCoords.getY();

            x = getLayoutX();
            y = getLayoutY();

            toFront();
        }
    }

    /**
     * Check if we can resize this node.
     *
     * @param event the mouse event.
     * @return true if we can start to resize.
     */
    @FXThread
    protected boolean isInResizableZone(@NotNull final MouseEvent event) {
        return event.getX() > (getWidth() - RESIZE_MARGIN);
    }

    /**
     * @return true if this node is dragging now.
     */
    @FXThread
    protected boolean isDragging() {
        return dragging;
    }

    /**
     * @param dragging true if this node is dragging now.
     */
    @FXThread
    protected void setDragging(final boolean dragging) {
        this.dragging = dragging;
    }

    /**
     * @return true if this node is resizing now.
     */
    @FXThread
    protected boolean isResizing() {
        return resizing;
    }

    /**
     * @param resizing true if this node is resizing now.
     */
    @FXThread
    protected void setResizing(final boolean resizing) {
        this.resizing = resizing;
    }
}
