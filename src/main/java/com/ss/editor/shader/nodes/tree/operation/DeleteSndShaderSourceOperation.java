package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSources;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to delete a shader source.
 *
 * @author JavaSaBr
 */
public class DeleteSndShaderSourceOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The shader node definition shader sources.
     */
    @NotNull
    private final SndShaderSources shaderSources;

    /**
     * The shader source.
     */
    @NotNull
    private final SndShaderSource shaderSource;

    /**
     * The previous position.
     */
    private int index;

    public DeleteSndShaderSourceOperation(@NotNull final SndShaderSources shaderSources,
                                          @NotNull final SndShaderSource shaderSource) {
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
