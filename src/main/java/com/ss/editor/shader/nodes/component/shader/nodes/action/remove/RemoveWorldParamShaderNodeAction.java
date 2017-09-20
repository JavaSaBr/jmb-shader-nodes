package com.ss.editor.shader.nodes.component.shader.nodes.action.remove;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.nodes.operation.remove.RemoveWorldParameterVariableOperation;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.component.shader.nodes.main.MainShaderNodeElement;
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
    @FXThread
    protected @NotNull String getName() {
        return PluginMessages.ACTION_DELETE;
    }

    @Override
    @FXThread
    protected void process() {
        super.process();

        final ShaderNodesContainer container = getContainer();
        final TechniqueDef techniqueDef = container.getTechniqueDef();
        final ShaderNodeVariable variable = getObject();
        final UniformBinding binding = UniformBinding.valueOf(variable.getName());

        final List<ShaderNode> usingNodes = container.findWithRightInputVar(variable, MainShaderNodeElement.class);

        final ShaderNodesChangeConsumer consumer = container.getChangeConsumer();
        consumer.execute(new RemoveWorldParameterVariableOperation(usingNodes, techniqueDef,
                binding, variable, getLocation()));
    }
}
