package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.remove;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The implementation of removing an attribute.
 *
 * @author JavaSaBr
 */
public class RemoveAttributeVariableOperation extends RemoveVariableOperation {

    public RemoveAttributeVariableOperation(@NotNull final List<ShaderNode> shaderNodes,
                                            @NotNull final TechniqueDef techniqueDef,
                                            @NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {
        super(shaderNodes, techniqueDef, variable, location);
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        info.getAttributes().remove(variable);
    }

    @Override
    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFxThread(editor);
        editor.notifyRemovedAttribute(variable);
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        info.getAttributes().add(variable);
    }

    @Override
    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFxThread(editor);
        editor.notifyAddedAttribute(variable, location);
    }
}
