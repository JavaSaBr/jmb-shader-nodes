package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of the {@link AbstractEditorOperation} to edit shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodeOperation extends AbstractEditorOperation<ShaderNodesChangeConsumer> {

    @Override
    @FxThread
    protected void redoImpl(@NotNull final ShaderNodesChangeConsumer editor) {
        EXECUTOR_MANAGER.addJmeTask(() -> {
            redoImplInJMEThread(editor);
            EXECUTOR_MANAGER.addFxTask(() -> redoImplInFXThread(editor));
        });
    }

    @JmeThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @FxThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @Override
    @FxThread
    protected void undoImpl(@NotNull final ShaderNodesChangeConsumer editor) {
        EXECUTOR_MANAGER.addJmeTask(() -> {
            undoImplInJMEThread(editor);
        EXECUTOR_MANAGER.addFxTask(() -> undoImplInFXThread(editor));
    });
    }

    @JmeThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @FxThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }
}
