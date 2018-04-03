package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_PARAMETER;
import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_PARAMETER_SOCKET;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of shader nodes parameter.
 *
 * @author JavaSaBr
 */
public class ShaderNodeParameter extends GridPane {

    /**
     * The shader nodes element.
     */
    @NotNull
    private final ShaderNodeElement<?> nodeElement;

    /**
     * The variable.
     */
    @NotNull
    private final ShaderNodeVariable variable;

    /**
     * The socket.
     */
    @NotNull
    private final SocketElement socket;

    /**
     * The name label.
     */
    @NotNull
    private final Label nameLabel;

    /**
     * The type label.
     */
    @NotNull
    private final Label typeLabel;

    public ShaderNodeParameter(@NotNull ShaderNodeElement<?> nodeElement, @NotNull ShaderNodeVariable variable) {
        this.nodeElement = nodeElement;
        this.variable = variable;
        this.socket = createSocket();
        this.nameLabel = new Label();
        this.typeLabel = new Label();
        createContent();
        FXUtils.addClassTo(this, SHADER_NODE_PARAMETER);
        FXUtils.addClassTo(socket, SHADER_NODE_PARAMETER_SOCKET);
    }

    /**
     * Refresh this parameter.
     */
    @FxThread
    public void refresh() {
    }

    /**
     * Get the shader nodes element.
     *
     * @return the shader nodes element.
     */
    @FxThread
    public @NotNull ShaderNodeElement<?> getNodeElement() {
        return nodeElement;
    }

    /**
     * Create a socket element.
     *
     * @return the socket element.
     */
    @FxThread
    protected @NotNull SocketElement createSocket() {
        return new SocketElement(this);
    }

    /**
     * Get the type label.
     *
     * @return the type label.
     */
    @FxThread
    protected @NotNull Label getTypeLabel() {
        return typeLabel;
    }

    /**
     * Get the name label.
     *
     * @return the name label.
     */
    @FxThread
    protected @NotNull Label getNameLabel() {
        return nameLabel;
    }

    /**
     * Get the socket.
     *
     * @return the socket.
     */
    @FxThread
    public @NotNull SocketElement getSocket() {
        return socket;
    }

    /**
     * Get the variable.
     *
     * @return the variable.
     */
    @FxThread
    public @NotNull ShaderNodeVariable getVariable() {
        return variable;
    }

    /**
     * Create content of this parameter.
     */
    @FxThread
    protected void createContent() {
        var variable = getVariable();
        getNameLabel().setText(variable.getName());
        getTypeLabel().setText(variable.getType());
    }
}
