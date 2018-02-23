package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.ShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
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
     * The shader nodes variable.
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
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> attributes = generationInfo.getAttributes();
        attributes.add(variable);
    }

    @Override
    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFxThread(editor);
        editor.notifyAddedAttribute(variable, location);
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> attributes = generationInfo.getAttributes();
        attributes.remove(variable);
    }

    @Override
    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFxThread(editor);
        editor.notifyRemovedAttribute(variable);
    }
}
