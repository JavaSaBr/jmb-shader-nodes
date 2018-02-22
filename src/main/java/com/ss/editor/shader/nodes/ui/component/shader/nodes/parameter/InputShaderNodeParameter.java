package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_INPUT_PARAMETER;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.InputSocketElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.rlib.ui.util.FXUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of input shader nodes parameter.
 *
 * @author JavaSaBr
 */
public class InputShaderNodeParameter extends ShaderNodeParameter {

    public InputShaderNodeParameter(@NotNull final ShaderNodeElement<?> nodeElement,
                                    @NotNull final ShaderNodeVariable variable) {
        super(nodeElement, variable);
        FXUtils.addClassTo(this, SHADER_NODE_INPUT_PARAMETER);
    }

    @Override
    @FxThread
    protected @NotNull SocketElement createSocket() {
        return new InputSocketElement(this);
    }

    @Override
    @FxThread
    protected void createContent() {
        super.createContent();
        FXUtils.addToPane(getSocket(), this);
        FXUtils.addToPane(getNameLabel(), this);
        FXUtils.addToPane(getTypeLabel(), this);
    }
}
