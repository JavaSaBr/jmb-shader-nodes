package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of attaching a variable to a shader nodes.
 *
 * @author JavaSaBr
 */
public class AttachVarExpressionToShaderNodeOperation extends AttachShaderNodeOperation {

    public AttachVarExpressionToShaderNodeOperation(
            @NotNull final ShaderNode shaderNode,
            @Nullable final VariableMapping newMapping,
            @Nullable final VariableMapping oldMapping
    ) {
        super(shaderNode, newMapping, oldMapping);
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);

        var shaderNode = getShaderNode();
        var newMapping = getNewMapping();
        var inputMapping = shaderNode.getInputMapping();

        if (getOldMapping() != null) {
            inputMapping.remove(getOldMapping());
        }

        if (newMapping != null) {
            inputMapping.add(newMapping);
        }
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);

        var newMapping = getNewMapping();
        var inputMapping = getShaderNode().getInputMapping();

        if (newMapping != null) {
            inputMapping.remove(newMapping);
        }

        if (getOldMapping() != null) {
            inputMapping.add(getOldMapping());
        }
    }
}
