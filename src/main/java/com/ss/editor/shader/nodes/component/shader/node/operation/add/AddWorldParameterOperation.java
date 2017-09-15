package com.ss.editor.shader.nodes.component.shader.node.operation.add;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.node.operation.ShaderNodeOperation;
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
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);
        techniqueDef.getWorldBindings().add(binding);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyAddedWorldParameter(binding, location);
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);
        techniqueDef.getWorldBindings().remove(binding);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyRemovedWorldParameter(binding);
    }
}
