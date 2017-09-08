package com.ss.editor.shader.nodes.editor.shader.node.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_NODE_OUTPUT_PARAMETER;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.socket.OutputSocketElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.socket.SocketElement;
import com.ss.rlib.ui.util.FXUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of output shader node parameter.
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
    protected @NotNull SocketElement createSocket() {
        return new OutputSocketElement(this);
    }

    @Override
    protected void createContent() {
        super.createContent();

        FXUtils.addToPane(getTypeLabel(), this);
        FXUtils.addToPane(getNameLabel(), this);
        FXUtils.addToPane(getSocket(), this);
    }
}
