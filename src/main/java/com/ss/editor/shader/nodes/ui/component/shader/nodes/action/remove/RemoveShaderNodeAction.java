package com.ss.editor.shader.nodes.ui.component.shader.nodes.action.remove;

import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.remove.RemoveShaderNodeOperation;
import com.ss.rlib.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * The action to delete an old world param.
 *
 * @author JavaSaBr
 */
public class RemoveShaderNodeAction extends ShaderNodeAction<ShaderNode> {

    public RemoveShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                  @NotNull final ShaderNode shaderNode,
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

        var container = getContainer();
        var techniqueDef = container.getTechniqueDef();
        var shaderNode = getObject();
        var usingNodes = new ArrayList<ShaderNode>();

        var definition = shaderNode.getDefinition();
        var outputs = definition.getOutputs();

        for (var outVar : outputs) {
            var toFind = Utils.get(outVar::clone);
            toFind.setNameSpace(shaderNode.getName());
            usingNodes.addAll(container.findWithRightInputVar(toFind, MainShaderNodeElement.class));
        }

        container.getChangeConsumer()
            .execute(new RemoveShaderNodeOperation(usingNodes, techniqueDef, shaderNode, getLocation()));
    }
}
