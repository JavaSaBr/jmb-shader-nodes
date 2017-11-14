package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSources;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to add a parameter.
 *
 * @author JavaSaBr
 */
public class AddSndShaderSourceOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The shader node shader sources.
     */
    @NotNull
    private final SndShaderSources shaderSources;

    /**
     * The shader source.
     */
    @NotNull
    private final SndShaderSource shaderSource;

    public AddSndShaderSourceOperation(@NotNull final SndShaderSources shaderSources,
                                       @NotNull final SndShaderSource shaderSource) {
        this.shaderSources = shaderSources;
        this.shaderSource = shaderSource;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        shaderSources.add(shaderSource);
        editor.notifyFXAddedChild(shaderSources, shaderSource, -1, true);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        shaderSources.remove(shaderSource);
        editor.notifyFXRemovedChild(shaderSources, shaderSources);
    }
}
