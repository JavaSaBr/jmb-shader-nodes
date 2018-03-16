package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_OUTPUT_PARAMETER;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.OutputSocketElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.rlib.ui.util.FXUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of output shader nodes parameter.
 *
 * @author JavaSaBr
 */
public class OutputShaderNodeParameter extends ShaderNodeParameter {

    public OutputShaderNodeParameter(@NotNull final ShaderNodeElement<?> nodeElement,
                                     @NotNull final ShaderNodeVariable variable) {
        super(nodeElement, variable);
        FXUtils.addClassTo(this, SHADER_NODE_OUTPUT_PARAMETER);
    }

    @Override
    @FxThread
    protected @NotNull SocketElement createSocket() {
        return new OutputSocketElement(this);
    }

    @Override
    @FxThread
    protected void createContent() {
        super.createContent();
        add(getTypeLabel(), 0, 0);
        add(getNameLabel(), 1, 0);
        add(getSocket(), 2, 0);
    }
}
