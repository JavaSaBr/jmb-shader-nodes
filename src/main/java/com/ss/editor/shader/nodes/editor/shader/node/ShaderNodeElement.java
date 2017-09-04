package com.ss.editor.shader.nodes.editor.shader.node;

import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE;
import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE_HEADER;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.rlib.ui.util.FXUtils;
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
        FXUtils.addClassTo(this, CSS_SHADER_NODE);
    }

    /**
     * @return the shader object.
     */
    protected @NotNull T getObject() {
        return object;
    }

    /**
     * Get the title text of this node.
     *
     * @return the title text of this node.
     */
    protected @NotNull String getTitleText() {
        return "Title";
    }

    /**
     * Create UI content of this node.
     */
    protected void createContent() {

        final StackPane header = new StackPane();
        final Label titleLabel = new Label(getTitleText());

        FXUtils.addClassTo(header, CSS_SHADER_NODE_HEADER);

        FXUtils.addToPane(titleLabel, header);
        FXUtils.addToPane(header, this);
        FXUtils.addToPane(parametersContainer, this);

        fillParameters(parametersContainer);
    }

    /**
     * Fill parameters of this node.
     *
     * @param container the parameters container.
     */
    protected void fillParameters(@NotNull final VBox container) {
    }

    /**
     * Handle mouse dragged event.
     *
     * @param event the mouse event.
     */
    private void handleMouseDragged(@NotNull final MouseEvent event) {

        if (isResizing()) {
            final double mouseX = event.getX();
            setPrefWidth(getPrefWidth() + (mouseX - x));
            x = mouseX;
        } else {

            final Parent parent = getParent();
            final Point2D parentCoords = parent.sceneToLocal(event.getSceneX(), event.getSceneY());

            double offsetX = parentCoords.getX() - mouseX;
            double offsetY = parentCoords.getY() - mouseY;

            x += offsetX;
            y += offsetY;

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
    private void handleMouseReleased(@NotNull final MouseEvent event) {
        if (isResizing()) {
            setResizing(false);
            setCursor(Cursor.DEFAULT);
        } else if (isDragging()) {
            setDragging(false);
        }
    }

    /**
     * Handle mouse moved event.
     *
     * @param event the mouse moved event.
     */
    private void handleMouseMoved(@NotNull final MouseEvent event) {
        if (isInResizableZone(event) || isResizing()) {
            setCursor(Cursor.W_RESIZE);
        } else {
            setCursor(Cursor.DEFAULT);
        }
    }

    public void setSelected(final boolean selected) {
        this.selected.setValue(selected);
    }

    /**
     * Handle mouse pressed event.
     *
     * @param event the mouse pressed event.
     */
    private void handleMousePressed(@NotNull final MouseEvent event) {

        if (event.getButton() != MouseButton.MIDDLE) {
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
    protected boolean isInResizableZone(@NotNull final MouseEvent event) {
        return event.getX() > (getWidth() - RESIZE_MARGIN);
    }

    /**
     * @return true if this node is dragging now.
     */
    protected boolean isDragging() {
        return dragging;
    }

    /**
     * @param dragging true if this node is dragging now.
     */
    protected void setDragging(final boolean dragging) {
        this.dragging = dragging;
    }

    /**
     * @return true if this node is resizing now.
     */
    protected boolean isResizing() {
        return resizing;
    }

    /**
     * @param resizing true if this node is resizing now.
     */
    protected void setResizing(final boolean resizing) {
        this.resizing = resizing;
    }
}
