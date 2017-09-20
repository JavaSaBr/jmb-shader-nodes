package com.ss.editor.shader.nodes.component.shader.nodes.operation.attach;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.nodes.operation.ShaderNodeOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base implementation of operation to attach variables.
 *
 * @author JavaSaBr
 */
public class AttachShaderNodeOperation extends ShaderNodeOperation {

    /**
     * The shader nodes.
     */
    @NotNull
    private final ShaderNode shaderNode;

    /**
     * The new mapping.
     */
    @Nullable
    private final VariableMapping newMapping;

    /**
     * The old mapping.
     */
    @Nullable
    private final VariableMapping oldMapping;

    protected AttachShaderNodeOperation(@NotNull final ShaderNode shaderNode,
                                        @Nullable final VariableMapping newMapping,
                                        @Nullable final VariableMapping oldMapping) {
        this.shaderNode = shaderNode;
        this.newMapping = newMapping;
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

    /**
     * Get the new mapping.
     *
     * @return the new mapping.
     */
    @FromAnyThread
    protected @Nullable VariableMapping getNewMapping() {
        return newMapping;
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        notify(editor, oldMapping, newMapping);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        notify(editor, newMapping, oldMapping);
    }

    @FXThread
    protected void notify(@NotNull final ShaderNodesChangeConsumer editor, @Nullable final VariableMapping oldMapping,
                          @Nullable final VariableMapping newMapping) {

        if (oldMapping != null && newMapping != null) {
            editor.notifyReplacedMapping(shaderNode, oldMapping, newMapping);
        } else if (oldMapping != null) {
            editor.notifyRemovedMapping(shaderNode, oldMapping);
        } else if (newMapping != null) {
            editor.notifyAddedMapping(shaderNode, newMapping);
        }
    }
}
