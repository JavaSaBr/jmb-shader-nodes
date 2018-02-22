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
            redoImplInJmeThread(editor);
            EXECUTOR_MANAGER.addFxTask(() -> redoImplInFxThread(editor));
        });
    }

    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @Override
    @FxThread
    protected void undoImpl(@NotNull final ShaderNodesChangeConsumer editor) {
        EXECUTOR_MANAGER.addJmeTask(() -> {
            undoImplInJmeThread(editor);
        EXECUTOR_MANAGER.addFxTask(() -> undoImplInFxThread(editor));
    });
    }

    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }

    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {

    }
}
