package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add;

import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.ShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The implementation of adding new technique.
 *
 * @author JavaSaBr
 */
public class AddTechniqueOperation extends ShaderNodeOperation {

    /**
     * The material definition.
     */
    @NotNull
    private final MaterialDef materialDef;

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    public AddTechniqueOperation(@NotNull final MaterialDef materialDef, @NotNull final TechniqueDef techniqueDef) {
        this.materialDef = materialDef;
        this.techniqueDef = techniqueDef;
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);
        materialDef.addTechniqueDef(techniqueDef);
    }

    @Override
    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFxThread(editor);
        editor.notifyAddedTechnique(techniqueDef);
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);

        final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(techniqueDef.getName());
        techniqueDefs.remove(techniqueDef);
    }

    @Override
    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFxThread(editor);
        editor.notifyRemovedTechnique(techniqueDef);
    }
}
