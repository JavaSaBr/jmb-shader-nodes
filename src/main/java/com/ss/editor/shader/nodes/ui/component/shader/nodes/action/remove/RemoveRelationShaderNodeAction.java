package com.ss.editor.shader.nodes.ui.component.shader.nodes.action.remove;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingByNNLeftVar;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findOutMappingByNNLeftVar;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.line.VariableLine;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.detach.InputDetachShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.detach.OutputDetachShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * The action to delete a relation.
 *
 * @author JavaSaBr
 */
public class RemoveRelationShaderNodeAction extends ShaderNodeAction<VariableLine> {

    public RemoveRelationShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                          @NotNull final VariableLine variableLine,
                                          @NotNull final Vector2f location) {
        super(container, variableLine, location);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_REMOVE;
    }

    @Override
    @FxThread
    public void process() {
        super.process();

        var container = getContainer();
        var variableLine = getObject();

        var inParameter = variableLine.getInParameter();
        var nodeElement = inParameter.getNodeElement();

        var outParameter = variableLine.getOutParameter();
        var outNodeElement = outParameter.getNodeElement();

        var consumer = container.getChangeConsumer();

        if (nodeElement instanceof OutputGlobalShaderNodeElement && outNodeElement instanceof MainShaderNodeElement) {

            var shaderNode = ((MainShaderNodeElement) outNodeElement).getObject();
            var mapping = notNull(findOutMappingByNNLeftVar(shaderNode, inParameter.getVariable()));

            consumer.execute(new OutputDetachShaderNodeOperation(shaderNode, mapping));

        } else if (nodeElement instanceof MainShaderNodeElement) {

            var shaderNode = ((MainShaderNodeElement) nodeElement).getObject();
            var mapping = notNull(findInMappingByNNLeftVar(shaderNode,
                    inParameter.getVariable(), shaderNode.getName()));

            consumer.execute(new InputDetachShaderNodeOperation(shaderNode, mapping));
        }
    }
}
