package com.ss.editor.shader.nodes.tree.operation;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionParameters;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to rename a parameter.
 *
 * @author JavaSaBr
 */
public class RenameShaderNodeDefinitionParameterOperation extends AbstractEditorOperation<ChangeConsumer> {

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
    private final ShaderNodeDefinitionParameters parameters;

    /**
     * The parameter.
     */
    @NotNull
    private final ShaderNodeVariable variable;

    public RenameShaderNodeDefinitionParameterOperation(@NotNull final String oldName, @NotNull final String newName,
                                                        @NotNull final ShaderNodeDefinitionParameters parameters,
                                                        @NotNull final ShaderNodeVariable variable) {
        this.oldName = oldName;
        this.newName = newName;
        this.parameters = parameters;
        this.variable = variable;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        variable.setName(newName);
        editor.notifyFXChangeProperty(parameters, variable, Messages.MODEL_PROPERTY_NAME);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        variable.setName(oldName);
        editor.notifyFXChangeProperty(parameters, variable, Messages.MODEL_PROPERTY_NAME);
    }
}
