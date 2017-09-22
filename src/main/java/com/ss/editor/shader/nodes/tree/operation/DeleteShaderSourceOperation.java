package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeShaderSources;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to delete a parameter.
 *
 * @author JavaSaBr
 */
public class DeleteShaderSourceOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The shader node shader sources.
     */
    @NotNull
    private final ShaderNodeShaderSources shaderSources;

    /**
     * The shader source.
     */
    @NotNull
    private final ShaderNodeShaderSource shaderSource;

    /**
     * THe previous position.
     */
    private int index;

    public DeleteShaderSourceOperation(@NotNull final ShaderNodeShaderSources shaderSources,
                                       @NotNull final ShaderNodeShaderSource shaderSource) {
        this.shaderSources = shaderSources;
        this.shaderSource = shaderSource;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        index = shaderSources.indexOf(shaderSource);
        shaderSources.remove(shaderSource);
        editor.notifyFXRemovedChild(shaderSources, shaderSource);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        shaderSources.add(index, shaderSource);
        editor.notifyFXAddedChild(shaderSources, shaderSource, index, false);
    }
}
