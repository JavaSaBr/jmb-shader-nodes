package com.ss.editor.shader.nodes.ui.control.tree.operation;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndParameters;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The operation to add a parameter.
 *
 * @author JavaSaBr
 */
public class AddSndParameterOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The definition parameters.
     */
    @NotNull
    private final SndParameters parameters;

    /**
     * The added parameter.
     */
    @NotNull
    private final ShaderNodeVariable variable;

    public AddSndParameterOperation(@NotNull final SndParameters parameters,
                                    @NotNull final ShaderNodeVariable variable) {
        this.parameters = parameters;
        this.variable = variable;
    }

    @Override
    @FxThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        final List<ShaderNodeVariable> parameterList = parameters.getParameters();
        parameterList.add(variable);
        editor.notifyFXAddedChild(parameters, variable, -1, true);
    }

    @Override
    @FxThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        final List<ShaderNodeVariable> parameterList = parameters.getParameters();
        parameterList.remove(variable);
        editor.notifyFXRemovedChild(parameters, variable);
    }
}
