package com.ss.editor.shader.nodes.editor.operation.remove;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The implementation of removing an attribute.
 *
 * @author JavaSaBr
 */
public class RemoveAttributeOperation extends RemoveOperation {

    public RemoveAttributeOperation(@NotNull final List<ShaderNode> shaderNodes,
                                    @NotNull final TechniqueDef techniqueDef,
                                    @NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {
        super(shaderNodes, techniqueDef, variable, location);
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        info.getAttributes().remove(variable);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyRemovedAttribute(variable);
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        info.getAttributes().add(variable);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyAddedAttribute(variable, location);
    }
}
