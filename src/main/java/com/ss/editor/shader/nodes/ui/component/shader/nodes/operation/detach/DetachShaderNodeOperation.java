package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.detach;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.ShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
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
     * The shader nodes.
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
     * Get the shader nodes.
     *
     * @return the shader nodes.
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
    @JmeThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);
        getMappings().remove(oldMapping);
    }

    @Override
    @FxThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyRemovedMapping(shaderNode, oldMapping);
    }

    /**
     * Get the mapping list.
     *
     * @return the mapping list.
     */
    @JmeThread
    protected @NotNull List<VariableMapping> getMappings() {
        throw new RuntimeException();
    }

    @Override
    @JmeThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);
        getMappings().add(oldMapping);
    }

    @Override
    @FxThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyAddedMapping(shaderNode, oldMapping);
    }
}
