package com.ss.editor.shader.nodes.editor.operation.detach;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.JMEThread;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The implementation of detaching input var mapping.
 *
 * @author JavaSaBr
 */
public class InputDetachShaderNodeOperation extends DetachShaderNodeOperation {

    public InputDetachShaderNodeOperation(@NotNull final ShaderNode shaderNode,
                                          @NotNull final VariableMapping oldMapping) {
        super(shaderNode, oldMapping);
    }

    @Override
    @JMEThread
    protected @NotNull List<VariableMapping> getMappings() {
        return getShaderNode().getInputMapping();
    }
}
