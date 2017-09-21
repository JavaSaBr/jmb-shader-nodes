package com.ss.editor.shader.nodes.tree.operation;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeParameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The operation to delete a parameter.
 *
 * @author JavaSaBr
 */
public class DeleteParameterOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The definition parameters.
     */
    @NotNull
    private final ShaderNodeParameters parameters;

    /**
     * The deleted parameter.
     */
    @NotNull
    private final ShaderNodeVariable variable;

    /**
     * The index of deleted parameter.
     */
    private int index;

    public DeleteParameterOperation(@NotNull final ShaderNodeParameters parameters,
                                    @NotNull final ShaderNodeVariable variable) {
        this.parameters = parameters;
        this.variable = variable;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        final List<ShaderNodeVariable> parameterList = parameters.getParameters();
        index = parameterList.indexOf(variable);
        parameterList.remove(variable);
        editor.notifyFXRemovedChild(parameters, variable);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        final List<ShaderNodeVariable> parameterList = parameters.getParameters();
        parameterList.add(index, variable);
        editor.notifyFXAddedChild(parameters, variable, index, false);
    }
}
