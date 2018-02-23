package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.ShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of adding new world parameter.
 *
 * @author JavaSaBr
 */
public class AddWorldParameterOperation extends ShaderNodeOperation {

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * The world parameter.
     */
    @NotNull
    private final UniformBinding binding;

    /**
     * The location.
     */
    @NotNull
    private final Vector2f location;

    public AddWorldParameterOperation(@NotNull final TechniqueDef techniqueDef, @NotNull final UniformBinding binding,
                                      @NotNull final Vector2f location) {
        this.techniqueDef = techniqueDef;
        this.binding = binding;
        this.location = location;
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);
        techniqueDef.getWorldBindings().add(binding);
    }

    @Override
    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFxThread(editor);
        editor.notifyAddedWorldParameter(binding, location);
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);
        techniqueDef.getWorldBindings().remove(binding);
    }

    @Override
    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFxThread(editor);
        editor.notifyRemovedWorldParameter(binding);
    }
}
