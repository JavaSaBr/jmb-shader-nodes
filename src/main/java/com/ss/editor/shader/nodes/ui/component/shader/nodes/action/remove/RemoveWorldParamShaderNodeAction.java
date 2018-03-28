package com.ss.editor.shader.nodes.ui.component.shader.nodes.action.remove;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.remove.RemoveWorldParameterVariableOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The action to delete an old world param.
 *
 * @author JavaSaBr
 */
public class RemoveWorldParamShaderNodeAction extends ShaderNodeAction<ShaderNodeVariable> {

    public RemoveWorldParamShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                            @NotNull final ShaderNodeVariable variable,
                                            @NotNull final Vector2f location) {
        super(container, variable, location);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_REMOVE;
    }

    @Override
    @FxThread
    protected void process() {
        super.process();

        var container = getContainer();
        var techniqueDef = container.getTechniqueDef();
        var variable = getObject();
        var binding = UniformBinding.valueOf(variable.getName());
        var usingNodes = container.findWithRightInputVar(variable, MainShaderNodeElement.class);

        container.getChangeConsumer()
            .execute(new RemoveWorldParameterVariableOperation(usingNodes, techniqueDef,
                binding, variable, getLocation()));
    }
}
