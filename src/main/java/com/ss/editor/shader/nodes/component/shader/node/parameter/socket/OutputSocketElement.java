package com.ss.editor.shader.nodes.component.shader.node.parameter.socket;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODE_PARAMETER_OUTPUT_SOCKET;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.component.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.node.parameter.ShaderNodeParameter;
import com.ss.rlib.ui.util.FXUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.input.*;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of output socket element.
 *
 * @author JavaSaBr
 */
public class OutputSocketElement extends SocketElement {

    @NotNull
    private static final DataFormat DATA_FORMAT = new DataFormat("java/shader_variable_node");

    @NotNull
    private static final PseudoClass DRAGGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("dragged");

    /**
     * The dragged state.
     */
    @NotNull
    private final BooleanProperty dragged = new BooleanPropertyBase(false) {

        @Override
        public void invalidated() {
            pseudoClassStateChanged(DRAGGED_PSEUDO_CLASS, get());
        }

        @Override
        public Object getBean() {
            return OutputSocketElement.this;
        }

        @Override
        public String getName() {
            return "dragged";
        }
    };

    public OutputSocketElement(@NotNull final ShaderNodeParameter parameter) {
        super(parameter);
        setOnDragDetected(this::handleStartDrag);
        setOnDragDone(this::handleStopDrag);
        setOnMouseDragged(this::handleMouseDragged);
        FXUtils.addClassTo(this, SHADER_NODE_PARAMETER_OUTPUT_SOCKET);
    }

    /**
     * Handle dragging.
     *
     * @param mouseEvent the mouse event.
     */
    @FXThread
    private void handleMouseDragged(final MouseEvent mouseEvent) {
        final ShaderNodeParameter parameter = getParameter();
        final ShaderNodeElement<?> nodeElement = parameter.getNodeElement();
        final ShaderNodesContainer container = nodeElement.getContainer();
        container.startAttaching(this);
        container.updateAttaching(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        mouseEvent.consume();
    }

    /**
     * Handle stopping dragging.
     *
     * @param dragEvent the drag event.
     */
    @FXThread
    private void handleStopDrag(@NotNull final DragEvent dragEvent) {
        setCursor(Cursor.DEFAULT);

        final ShaderNodeParameter parameter = getParameter();
        final ShaderNodeElement<?> nodeElement = parameter.getNodeElement();
        final ShaderNodesContainer container = nodeElement.getContainer();
        container.finishAttaching();

        dragEvent.consume();
        dragged.setValue(false);
    }

    /**
     * Handle starting dragging.
     *
     * @param mouseEvent the mouse event.
     */
    @FXThread
    private void handleStartDrag(@NotNull final MouseEvent mouseEvent) {
        setCursor(Cursor.MOVE);

        final ClipboardContent content = new ClipboardContent();
        content.put(DATA_FORMAT, "");

        final Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
        dragBoard.setContent(content);

        final ShaderNodeParameter parameter = getParameter();
        final ShaderNodeElement<?> nodeElement = parameter.getNodeElement();
        final ShaderNodesContainer container = nodeElement.getContainer();
        container.startAttaching(this);
        container.updateAttaching(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        dragged.setValue(true);
        mouseEvent.consume();
    }
}
