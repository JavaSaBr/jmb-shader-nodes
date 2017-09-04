package com.ss.editor.shader.nodes.editor.shader.node.parameter;

import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE_PARAMETER;
import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE_PARAMETER_SOCKET;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of shader node parameter.
 *
 * @author JavaSaBr
 */
public class ShaderNodeParameter extends HBox {

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

    public ShaderNodeParameter(@NotNull final ShaderNodeElement<?> nodeElement, @NotNull final ShaderNodeVariable variable) {
        this.variable = variable;
        this.socket = new SocketElement(nodeElement);
        this.nameLabel = new Label();
        this.typeLabel = new Label();
        createContent();
        FXUtils.addClassTo(this, CSS_SHADER_NODE_PARAMETER);
        FXUtils.addClassTo(socket, CSS_SHADER_NODE_PARAMETER_SOCKET);
    }

    /**
     * @return the type label.
     */
    protected @NotNull Label getTypeLabel() {
        return typeLabel;
    }

    /**
     * @return the name label.
     */
    protected @NotNull Label getNameLabel() {
        return nameLabel;
    }

    /**
     * @return the socket.
     */
    public @NotNull SocketElement getSocket() {
        return socket;
    }

    /**
     * @return the variable.
     */
    public @NotNull ShaderNodeVariable getVariable() {
        return variable;
    }

    protected void createContent() {

        final ShaderNodeVariable variable = getVariable();

        final Label nameLabel = getNameLabel();
        nameLabel.setText(variable.getName());

        final Label typeLabel = getTypeLabel();
        typeLabel.setText(variable.getType());
    }
}
