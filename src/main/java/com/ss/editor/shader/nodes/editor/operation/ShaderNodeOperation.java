package com.ss.editor.shader.nodes.editor.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of the {@link AbstractEditorOperation} to edit shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodeOperation extends AbstractEditorOperation<ShaderNodesChangeConsumer> {

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ShaderNodesChangeConsumer editor) {
        EXECUTOR_MANAGER.addJMETask(() -> {
            redoImplInJMEThread(editor);
            EXECUTOR_MANAGER.addFXTask(() -> redoImplInFXThread(editor));
        });
    }

    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ShaderNodesChangeConsumer editor) {
        EXECUTOR_MANAGER.addJMETask(() -> {
            undoImplInJMEThread(editor);
        EXECUTOR_MANAGER.addFXTask(() -> undoImplInFXThread(editor));
    });
    }

    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }
}
