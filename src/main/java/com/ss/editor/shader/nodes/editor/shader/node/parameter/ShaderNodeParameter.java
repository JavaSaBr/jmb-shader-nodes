package com.ss.editor.shader.nodes.editor.shader.node.parameter;

import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE_PARAMETER;
import static com.ss.editor.shader.nodes.ShaderNodesEditorPlugin.CSS_SHADER_NODE_PARAMETER_SOCKET;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of shader node parameter.
 *
 * @author JavaSaBr
 */
public class ShaderNodeParameter extends HBox {

    @NotNull
    private final ShaderNodeVariable variable;

    @NotNull
    private final Pane socket;

    @NotNull
    private final Label nameLabel;

    @NotNull
    private final Label typeLabel;

    public ShaderNodeParameter(@NotNull final ShaderNodeVariable variable) {
        this.variable = variable;
        this.socket = new Pane();
        this.nameLabel = new Label();
        this.typeLabel = new Label();
        createContent();
        FXUtils.addClassTo(this, CSS_SHADER_NODE_PARAMETER);
        FXUtils.addClassTo(socket, CSS_SHADER_NODE_PARAMETER_SOCKET);
    }

    protected @NotNull Label getTypeLabel() {
        return typeLabel;
    }

    protected @NotNull Label getNameLabel() {
        return nameLabel;
    }

    public @NotNull Pane getSocket() {
        return socket;
    }

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
