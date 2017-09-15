package com.ss.editor.shader.nodes.component.shader.node.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODE_MATERIAL_OUTPUT_PARAMETER;
import com.jme3.material.MatParam;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.node.main.MaterialShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.node.parameter.socket.OutputSocketElement;
import com.ss.editor.shader.nodes.component.shader.node.parameter.socket.SocketElement;
import com.ss.editor.ui.control.property.PropertyControl;
import com.ss.rlib.ui.util.FXUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of output shader node parameter.
 *
 * @author JavaSaBr
 */
public class EditableMaterialShaderNodeParameter extends OutputShaderNodeParameter {

    /**
     * The property control.
     */
    @NotNull
    private final PropertyControl<ChangeConsumer, MatParam, ?> propertyControl;

    public EditableMaterialShaderNodeParameter(@NotNull final MaterialShaderNodeElement nodeElement,
                                               @NotNull final ShaderNodeVariable variable,
                                               @NotNull final PropertyControl<ChangeConsumer, MatParam, ?> propertyControl) {
        super(nodeElement, variable);
        this.propertyControl = propertyControl;

        propertyControl.prefWidthProperty().bind(widthProperty());

        FXUtils.addToPane(propertyControl, this);
        FXUtils.addToPane(getSocket(), this);
        FXUtils.addClassTo(this, SHADER_NODE_MATERIAL_OUTPUT_PARAMETER);
    }

    /**
     * Sync the current value of this parameter.
     */
    @FXThread
    public void sync() {
        propertyControl.sync();
    }

    @Override
    @FXThread
    protected @NotNull SocketElement createSocket() {
        return new OutputSocketElement(this);
    }

    @Override
    @FXThread
    protected void createContent() {
    }
}
