package com.ss.editor.shader.nodes.ui.component.shader.nodes.line;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODE_LINE;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of line between two variables.
 *
 * @author JavaSaBr
 */
public class VariableLine extends CubicCurve {

    @NotNull
    private final ShaderNodeParameter outParameter;

    @NotNull
    private final ShaderNodeParameter inParameter;

    public VariableLine(@NotNull final ShaderNodeParameter outParameter,
                        @NotNull final ShaderNodeParameter inParameter) {
        this.outParameter = outParameter;
        this.inParameter = inParameter;
        setOnContextMenuRequested(this::handleContextMenuRequested);
        configureLine();
        getStyleClass().add(SHADER_NODE_LINE);
    }

    /**
     * Handle context menu requested events.
     *
     * @param event the menu requested event.
     */
    @FXThread
    private void handleContextMenuRequested(@NotNull final ContextMenuEvent event) {
        final ShaderNodeElement<?> nodeElement = inParameter.getNodeElement();
        final ShaderNodesContainer container = nodeElement.getContainer();
        container.handleContextMenuEvent(event);
        event.consume();
    }

    /**
     * Get the input parameter.
     *
     * @return the input parameter.
     */
    @FXThread
    public @NotNull ShaderNodeParameter getInParameter() {
        return inParameter;
    }

    /**
     * Get the output parameter.
     *
     * @return the output parameter.
     */
    @FXThread
    public @NotNull ShaderNodeParameter getOutParameter() {
        return outParameter;
    }

    /**
     * Configure the line.
     */
    @FXThread
    private void configureLine() {

        final SocketElement outSocket = outParameter.getSocket();
        final SocketElement inSocket = inParameter.getSocket();

        startXProperty().bind(outSocket.centerXPropertyProperty());
        startYProperty().bind(outSocket.centerYPropertyProperty());
        endXProperty().bind(inSocket.centerXPropertyProperty());
        endYProperty().bind(inSocket.centerYPropertyProperty());
        controlX1Property().bind(startXProperty().add(60D));
        controlY1Property().bind(startYProperty().add(10D));
        controlX2Property().bind(endXProperty().subtract(60D));
        controlY2Property().bind(endYProperty().subtract(10D));
        setStrokeLineCap(StrokeLineCap.ROUND);
    }
}
