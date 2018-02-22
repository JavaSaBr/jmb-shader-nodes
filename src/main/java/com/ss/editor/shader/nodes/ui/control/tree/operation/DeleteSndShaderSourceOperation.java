package com.ss.editor.shader.nodes.ui.control.tree.operation;

import com.ss.editor.annotation.FxThread;
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
    @FxThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        index = shaderSources.indexOf(shaderSource);
        shaderSources.remove(shaderSource);
        editor.notifyFxRemovedChild(shaderSources, shaderSource);
    }

    @Override
    @FxThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        shaderSources.add(index, shaderSource);
        editor.notifyFxAddedChild(shaderSources, shaderSource, index, false);
    }
}
