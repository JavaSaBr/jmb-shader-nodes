package com.ss.editor.shader.nodes.ui.control.tree.operation;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndParameters;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to rename a parameter.
 *
 * @author JavaSaBr
 */
public class RenameSndParameterOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The old name.
     */
    @NotNull
    private final String oldName;

    /**
     * The new name.
     */
    @NotNull
    private final String newName;

    /**
     * The list of parameters.
     */
    @NotNull
    private final SndParameters parameters;

    /**
     * The parameter.
     */
    @NotNull
    private final ShaderNodeVariable variable;

    public RenameSndParameterOperation(@NotNull final String oldName, @NotNull final String newName,
                                       @NotNull final SndParameters parameters,
                                       @NotNull final ShaderNodeVariable variable) {
        this.oldName = oldName;
        this.newName = newName;
        this.parameters = parameters;
        this.variable = variable;
    }

    @Override
    @FxThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        variable.setName(newName);
        editor.notifyFxChangeProperty(parameters, variable, Messages.MODEL_PROPERTY_NAME);
    }

    @Override
    @FxThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        variable.setName(oldName);
        editor.notifyFxChangeProperty(parameters, variable, Messages.MODEL_PROPERTY_NAME);
    }
}
