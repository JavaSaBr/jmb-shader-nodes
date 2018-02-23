package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.remove;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
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
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);
        techniqueDef.getWorldBindings().remove(binding);
    }

    @Override
    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFxThread(editor);
        editor.notifyRemovedWorldParameter(binding);
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);
        techniqueDef.getWorldBindings().add(binding);
    }

    @Override
    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFxThread(editor);
        editor.notifyAddedWorldParameter(binding, location);
    }
}
