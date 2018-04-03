package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_PARAMETER_OUTPUT_SOCKET;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
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

    public OutputSocketElement(@NotNull ShaderNodeParameter parameter) {
        super(parameter);
        setOnDragDetected(this::handleStartDrag);
        setOnDragDone(this::handleStopDrag);
        setOnMouseDragged(this::handleMouseDragged);
        FXUtils.addClassTo(this, SHADER_NODE_PARAMETER_OUTPUT_SOCKET);
    }

    /**
     * Handle dragging.
     *
     * @param event the mouse event.
     */
    @FxThread
    private void handleMouseDragged(@NotNull MouseEvent event) {

        var container = getParameter()
            .getNodeElement()
            .getContainer();

        container.startAttaching(this);
        container.updateAttaching(event.getSceneX(), event.getSceneY());

        event.consume();
    }

    /**
     * Handle stopping dragging.
     *
     * @param dragEvent the drag event.
     */
    @FxThread
    private void handleStopDrag(@NotNull DragEvent dragEvent) {
        setCursor(Cursor.DEFAULT);

        getParameter().getNodeElement()
            .getContainer()
            .finishAttaching();

        dragEvent.consume();

        dragged.setValue(false);
    }

    /**
     * Handle starting dragging.
     *
     * @param mouseEvent the mouse event.
     */
    @FxThread
    private void handleStartDrag(@NotNull MouseEvent mouseEvent) {
        setCursor(Cursor.MOVE);

        var content = new ClipboardContent();
        content.put(DATA_FORMAT, "");

        var dragBoard = startDragAndDrop(TransferMode.MOVE);
        dragBoard.setContent(content);

        var parameter = getParameter();
        var nodeElement = parameter.getNodeElement();

        var container = nodeElement.getContainer();
        container.startAttaching(this);
        container.updateAttaching(mouseEvent.getSceneX(), mouseEvent.getSceneY());

        dragged.setValue(true);

        mouseEvent.consume();
    }
}
