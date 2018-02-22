package com.ss.editor.shader.nodes.ui.control.tree.operation;

import com.ss.editor.annotation.FxThread;
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
    @FxThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        shaderSources.add(shaderSource);
        editor.notifyFxAddedChild(shaderSources, shaderSource, -1, true);
    }

    @Override
    @FxThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        shaderSources.remove(shaderSource);
        editor.notifyFxRemovedChild(shaderSources, shaderSources);
    }
}
