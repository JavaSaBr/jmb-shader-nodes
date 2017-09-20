package com.ss.editor.shader.nodes.component.shader.nodes.action.remove;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingByNNLeftVar;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findOutMappingByNNLeftVar;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.nodes.operation.detach.InputDetachShaderNodeOperation;
import com.ss.editor.shader.nodes.component.shader.nodes.operation.detach.OutputDetachShaderNodeOperation;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.component.shader.nodes.global.OutputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.line.VariableLine;
import com.ss.editor.shader.nodes.component.shader.nodes.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.ShaderNodeParameter;
import org.jetbrains.annotations.NotNull;

/**
 * The action to delete a relation.
 *
 * @author JavaSaBr
 */
public class RemoveRelationShaderNodeAction extends ShaderNodeAction<VariableLine> {

    public RemoveRelationShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                          @NotNull final VariableLine variableLine, @NotNull final Vector2f location) {
        super(container, variableLine, location);
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
        final VariableLine variableLine = getObject();

        final ShaderNodeParameter inParameter = variableLine.getInParameter();
        final ShaderNodeElement<?> nodeElement = inParameter.getNodeElement();

        final ShaderNodeParameter outParameter = variableLine.getOutParameter();
        final ShaderNodeElement<?> outNodeElement = outParameter.getNodeElement();

        final ShaderNodesChangeConsumer consumer = container.getChangeConsumer();

        if (nodeElement instanceof OutputGlobalShaderNodeElement && outNodeElement instanceof MainShaderNodeElement) {

            final ShaderNode shaderNode = ((MainShaderNodeElement) outNodeElement).getObject();
            final VariableMapping mapping = notNull(findOutMappingByNNLeftVar(shaderNode, inParameter.getVariable()));

            consumer.execute(new OutputDetachShaderNodeOperation(shaderNode, mapping));

        } else if (nodeElement instanceof MainShaderNodeElement) {

            final ShaderNode shaderNode = ((MainShaderNodeElement) nodeElement).getObject();
            final VariableMapping mapping = notNull(findInMappingByNNLeftVar(shaderNode,
                    inParameter.getVariable(), shaderNode.getName()));

            consumer.execute(new InputDetachShaderNodeOperation(shaderNode, mapping));
        }
    }
}
