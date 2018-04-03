package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_MATERIAL_OUTPUT_PARAMETER;
import com.jme3.material.MatParam;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.MaterialShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.OutputSocketElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.editor.ui.control.property.PropertyControl;
import com.ss.rlib.ui.util.FXUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of output shader nodes parameter.
 *
 * @author JavaSaBr
 */
public class EditableMaterialShaderNodeParameter extends OutputShaderNodeParameter {

    /**
     * The property control.
     */
    @NotNull
    private final PropertyControl<ChangeConsumer, MatParam, ?> propertyControl;

    public EditableMaterialShaderNodeParameter(
        @NotNull MaterialShaderNodeElement nodeElement,
        @NotNull ShaderNodeVariable variable,
        @NotNull PropertyControl<ChangeConsumer, MatParam, ?> propertyControl
    ) {
        super(nodeElement, variable);
        this.propertyControl = propertyControl;

        propertyControl.prefWidthProperty().bind(widthProperty());

        add(propertyControl, 0, 0);
        add(getSocket(), 1, 0);

        FXUtils.addClassTo(this, SHADER_NODE_MATERIAL_OUTPUT_PARAMETER);
        refresh();
    }

    @Override
    public void refresh() {
        super.refresh();
        propertyControl.sync();
    }

    @Override
    @FxThread
    protected @NotNull SocketElement createSocket() {
        return new OutputSocketElement(this);
    }

    @Override
    @FxThread
    protected void createContent() {
    }
}
