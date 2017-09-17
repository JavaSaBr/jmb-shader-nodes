package com.ss.editor.shader.nodes.component.shader.node.action.remove;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.node.operation.remove.RemoveShaderNodeOperation;
import com.ss.editor.shader.nodes.component.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.node.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.component.shader.node.main.MainShaderNodeElement;
import com.ss.rlib.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The action to delete an old world param.
 *
 * @author JavaSaBr
 */
public class RemoveShaderNodeAction extends ShaderNodeAction<ShaderNode> {

    public RemoveShaderNodeAction(@NotNull final ShaderNodesContainer container, @NotNull final ShaderNode shaderNode,
                                  @NotNull final Vector2f location) {
        super(container, shaderNode, location);
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
        final ShaderNode shaderNode = getObject();

        final List<ShaderNode> usingNodes = new ArrayList<>();

        final ShaderNodeDefinition definition = shaderNode.getDefinition();
        final List<ShaderNodeVariable> outputs = definition.getOutputs();

        for (final ShaderNodeVariable outVar : outputs) {
            final ShaderNodeVariable toFind = Utils.get(outVar::clone);
            toFind.setNameSpace(shaderNode.getName());
            usingNodes.addAll(container.findWithRightInputVar(toFind, MainShaderNodeElement.class));
        }

        final ShaderNodesChangeConsumer consumer = container.getChangeConsumer();
        consumer.execute(new RemoveShaderNodeOperation(usingNodes, techniqueDef, shaderNode, getLocation()));
    }
}
