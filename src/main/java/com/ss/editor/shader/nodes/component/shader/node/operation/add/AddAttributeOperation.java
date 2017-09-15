package com.ss.editor.shader.nodes.component.shader.node.operation.add;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.node.operation.ShaderNodeOperation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The implementation of adding new attribute.
 *
 * @author JavaSaBr
 */
public class AddAttributeOperation extends ShaderNodeOperation {

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * The shader node variable.
     */
    @NotNull
    private final ShaderNodeVariable variable;

    /**
     * The location.
     */
    @NotNull
    private final Vector2f location;

    public AddAttributeOperation(@NotNull final TechniqueDef techniqueDef, @NotNull final ShaderNodeVariable variable,
                                 @NotNull final Vector2f location) {
        this.techniqueDef = techniqueDef;
        this.variable = variable;
        this.location = location;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> attributes = generationInfo.getAttributes();
        attributes.add(variable);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyAddedAttribute(variable, location);
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> attributes = generationInfo.getAttributes();
        attributes.remove(variable);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyRemovedAttribute(variable);
    }
}
