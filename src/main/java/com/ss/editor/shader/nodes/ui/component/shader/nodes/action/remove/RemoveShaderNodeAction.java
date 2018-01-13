package com.ss.editor.shader.nodes.ui.component.shader.nodes.action.remove;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.remove.RemoveShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
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
    @FxThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_REMOVE;
    }

    @Override
    @FxThread
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
