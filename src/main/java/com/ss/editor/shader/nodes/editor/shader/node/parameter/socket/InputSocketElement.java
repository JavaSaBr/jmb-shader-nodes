package com.ss.editor.shader.nodes.editor.shader.node.parameter.socket;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.CSS_SHADER_NODE_PARAMETER_INPUT_SOCKET;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import com.ss.rlib.ui.util.FXUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of input socket element.
 *
 * @author JavaSaBr
 */
public class InputSocketElement extends SocketElement {

    @NotNull
    private static final PseudoClass DRAGGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("droppable");

    /**
     * The droppable state.
     */
    @NotNull
    private final BooleanProperty droppable = new BooleanPropertyBase(false) {

        @Override
        public void invalidated() {
            pseudoClassStateChanged(DRAGGED_PSEUDO_CLASS, get());
        }

        @Override
        public Object getBean() {
            return InputSocketElement.this;
        }

        @Override
        public String getName() {
            return "droppable";
        }
    };

    public InputSocketElement(@NotNull final ShaderNodeParameter parameter) {
        super(parameter);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragExited(this::handleDragExited);
        FXUtils.addClassTo(this, CSS_SHADER_NODE_PARAMETER_INPUT_SOCKET);
    }

    /**
     * Handle exiting dragged object.
     *
     * @param dragEvent the drag event.
     */
    private void handleDragExited(@NotNull final DragEvent dragEvent) {
        droppable.setValue(false);
    }

    /**
     * Handle dropping object.
     *
     * @param dragEvent the drag event.
     */
    private void handleDragDropped(@NotNull final DragEvent dragEvent) {
        droppable.setValue(false);
    }

    /**
     * Handle dragging over object.
     *
     * @param dragEvent the drag event.
     */
    private void handleDragOver(@NotNull final DragEvent dragEvent) {

        final ShaderNodeParameter parameter = getParameter();
        final ShaderNodeElement<?> nodeElement = parameter.getNodeElement();
        final ShaderNodesContainer container = nodeElement.getContainer();
        container.updateAttaching(dragEvent.getSceneX(), dragEvent.getSceneY());

        final Object gestureSource = dragEvent.getGestureSource();
        if (!(gestureSource instanceof SocketElement)) {
            return;
        }

        final SocketElement element = (SocketElement) gestureSource;
        final ShaderNodeVariable sourceVar = element.getParameter().getVariable();
        final ShaderNodeVariable targetVar = parameter.getVariable();

        if (!sourceVar.getType().equals(targetVar.getType())) {
            return;
        }

        final Point2D scene = localToScene(getWidth() / 2, getHeight() / 2);
        container.updateAttaching(scene.getX(), scene.getY());

        dragEvent.acceptTransferModes(TransferMode.MOVE);
        dragEvent.consume();

        droppable.setValue(true);
    }
}
