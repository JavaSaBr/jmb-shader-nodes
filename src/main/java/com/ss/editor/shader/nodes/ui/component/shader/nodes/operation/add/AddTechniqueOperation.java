package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add;

import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
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
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);
        materialDef.addTechniqueDef(techniqueDef);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyAddedTechnique(techniqueDef);
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(techniqueDef.getName());
        techniqueDefs.remove(techniqueDef);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyRemovedTechnique(techniqueDef);
    }
}
