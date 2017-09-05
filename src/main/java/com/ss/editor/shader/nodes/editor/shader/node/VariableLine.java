package com.ss.editor.shader.nodes.editor.shader.node;

import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE_LINE;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.SocketElement;
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
        configureLine();
        getStyleClass().add(CSS_SHADER_NODE_LINE);
    }

    /**
     * Configure the line.
     */
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
