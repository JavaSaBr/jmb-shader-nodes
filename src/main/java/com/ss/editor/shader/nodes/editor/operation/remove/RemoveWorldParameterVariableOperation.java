package com.ss.editor.shader.nodes.editor.operation.remove;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The implementation of removing old world parameter.
 *
 * @author JavaSaBr
 */
public class RemoveWorldParameterVariableOperation extends RemoveUniformVariableOperation {

    /**
     * The world parameter.
     */
    @NotNull
    private final UniformBinding binding;

    public RemoveWorldParameterVariableOperation(@NotNull final List<ShaderNode> shaderNodes,
                                                 @NotNull final TechniqueDef techniqueDef, @NotNull final UniformBinding binding,
                                                 @NotNull final ShaderNodeVariable variable,
                                                 @NotNull final Vector2f location) {
        super(shaderNodes, techniqueDef, variable, location);
        this.binding = binding;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);
        techniqueDef.getWorldBindings().remove(binding);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyRemovedWorldParameter(binding);
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);
        techniqueDef.getWorldBindings().add(binding);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyAddedWorldParameter(binding, location);
    }
}
