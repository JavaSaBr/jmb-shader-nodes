package com.ss.editor.shader.nodes.component.shader.node.operation.add;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.node.operation.ShaderNodeOperation;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of adding new shader node.
 *
 * @author JavaSaBr
 */
public class AddShaderNodeOperation extends ShaderNodeOperation {

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * The shader node.
     */
    @NotNull
    private final ShaderNode shaderNode;

    /**
     * The location.
     */
    @NotNull
    private final Vector2f location;

    public AddShaderNodeOperation(@NotNull final TechniqueDef techniqueDef, @NotNull final ShaderNode shaderNode,
                                  @NotNull final Vector2f location) {
        this.techniqueDef = techniqueDef;
        this.shaderNode = shaderNode;
        this.location = location;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);
        techniqueDef.getShaderNodes().add(shaderNode);
        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        //info.getUnusedNodes().add(shaderNode.getName());
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyAddedShaderNode(shaderNode, location);
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);
        techniqueDef.getShaderNodes().remove(shaderNode);
        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        //info.getUnusedNodes().remove(shaderNode.getName());
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyRemovedRemovedShaderNode(shaderNode);
    }
}
