package com.ss.editor.shader.nodes.editor.operation.detach;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.operation.ShaderNodeOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The base implementation of operation to attach variables.
 *
 * @author JavaSaBr
 */
public class DetachShaderNodeOperation extends ShaderNodeOperation {

    /**
     * The shader node.
     */
    @NotNull
    private final ShaderNode shaderNode;

    /**
     * The old mapping.
     */
    @NotNull
    private final VariableMapping oldMapping;

    protected DetachShaderNodeOperation(@NotNull final ShaderNode shaderNode,
                                        @NotNull final VariableMapping oldMapping) {
        this.shaderNode = shaderNode;
        this.oldMapping = oldMapping;
    }

    /**
     * Get the shader node.
     *
     * @return the shader node.
     */
    @FromAnyThread
    protected @NotNull ShaderNode getShaderNode() {
        return shaderNode;
    }

    /**
     * Get the old mapping.
     *
     * @return the old mapping.
     */
    @FromAnyThread
    protected @Nullable VariableMapping getOldMapping() {
        return oldMapping;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);
        getMappings().remove(oldMapping);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyRemovedMapping(shaderNode, oldMapping);
    }

    /**
     * Get the mapping list.
     *
     * @return the mapping list.
     */
    @JMEThread
    protected @NotNull List<VariableMapping> getMappings() {
        throw new RuntimeException();
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);
        getMappings().add(oldMapping);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyAddedMapping(shaderNode, oldMapping);
    }
}
