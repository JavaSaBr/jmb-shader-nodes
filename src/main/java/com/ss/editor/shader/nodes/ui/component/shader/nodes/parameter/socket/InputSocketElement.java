package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_PARAMETER_INPUT_SOCKET;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.util.ShaderNodeUtils;
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
    private static final PseudoClass DROPPABLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("droppable");

    @NotNull
    private static final PseudoClass REQUIRED_PSEUDO_CLASS = PseudoClass.getPseudoClass("required");

    /**
     * The droppable state.
     */
    @NotNull
    private final BooleanProperty droppable = new BooleanPropertyBase(false) {

        @Override
        public void invalidated() {
            pseudoClassStateChanged(DROPPABLE_PSEUDO_CLASS, get());
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

    /**
     * The required state.
     */
    @NotNull
    private final BooleanProperty required = new BooleanPropertyBase(false) {

        @Override
        public void invalidated() {
            pseudoClassStateChanged(REQUIRED_PSEUDO_CLASS, get());
        }

        @Override
        public Object getBean() {
            return InputSocketElement.this;
        }

        @Override
        public String getName() {
            return "required";
        }
    };

    public InputSocketElement(@NotNull ShaderNodeParameter parameter) {
        super(parameter);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
        setOnDragExited(this::handleDragExited);
        required.setValue(ShaderNodeUtils.isRequired(parameter.getVariable()));
        FXUtils.addClassTo(this, SHADER_NODE_PARAMETER_INPUT_SOCKET);
    }

    /**
     * Handle exiting dragged object.
     *
     * @param dragEvent the drag event.
     */
    @FxThread
    private void handleDragExited(@NotNull DragEvent dragEvent) {
        droppable.setValue(false);
    }

    /**
     * Handle dropping object.
     *
     * @param dragEvent the drag event.
     */
    @FxThread
    private void handleDragDropped(@NotNull DragEvent dragEvent) {
        droppable.setValue(false);

        var parameter = (InputShaderNodeParameter) getParameter();
        var nodeElement = parameter.getNodeElement();

        var gestureSource = dragEvent.getGestureSource();
        if (!(gestureSource instanceof SocketElement)) {
            return;
        }

        var outputSocket = (SocketElement) gestureSource;
        var outputParameter = (OutputShaderNodeParameter) outputSocket.getParameter();

        if (!nodeElement.canAttach(parameter, outputParameter)) {
            return;
        }

        nodeElement.attach(parameter, outputParameter);
    }

    /**
     * Handle dragging over object.
     *
     * @param dragEvent the drag event.
     */
    @FxThread
    private void handleDragOver(@NotNull DragEvent dragEvent) {

        var parameter = (InputShaderNodeParameter) getParameter();
        var nodeElement = parameter.getNodeElement();
        var container = nodeElement.getContainer();
        container.updateAttaching(dragEvent.getSceneX(), dragEvent.getSceneY());

        var gestureSource = dragEvent.getGestureSource();
        if (!(gestureSource instanceof SocketElement)) {
            return;
        }

        var outputSocket = (SocketElement) gestureSource;
        if (!nodeElement.canAttach(parameter, (OutputShaderNodeParameter) outputSocket.getParameter())) {
            return;
        }

        var scene = localToScene(getWidth() / 2, getHeight() / 2);

        container.updateAttaching(scene.getX(), scene.getY());

        dragEvent.acceptTransferModes(TransferMode.MOVE);
        dragEvent.consume();

        droppable.setValue(true);
    }
}
