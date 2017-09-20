package com.ss.editor.shader.nodes.component.shader.nodes.line;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODE_TEMP_LINE;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.socket.SocketElement;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of temp line between one variable and mouse position.
 *
 * @author JavaSaBr
 */
public class TempLine extends CubicCurve {

    @NotNull
    private final SocketElement sourceSocket;

    public TempLine(@NotNull final SocketElement sourceSocket) {
        this.sourceSocket = sourceSocket;
        configureLine();
        getStyleClass().add(SHADER_NODE_TEMP_LINE);
    }

    /**
     * Configure the line.
     */
    @FXThread
    private void configureLine() {
        startXProperty().bind(sourceSocket.centerXPropertyProperty());
        startYProperty().bind(sourceSocket.centerYPropertyProperty());
        controlX1Property().bind(startXProperty().add(60D));
        controlY1Property().bind(startYProperty().add(10D));
        controlX2Property().bind(endXProperty().add(endXProperty().subtract(startXProperty())));
        controlX2Property().bind(endXProperty().subtract(60D));
        controlY2Property().bind(endYProperty().subtract(10D));
        setStrokeLineCap(StrokeLineCap.ROUND);
    }

    /**
     * Update the end of this line.
     *
     * @param x the X coord.
     * @param y the Y coord.
     */
    @FXThread
    public void updateEnd(final double x, final double y) {
        setEndX(x);
        setEndY(y);
    }
}
