package com.ss.editor.shader.nodes.editor.shader.node.global;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.calculateRightSwizzling;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findOutMappingByNNLeftVar;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.makeMapping;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.operation.attach.AttachVarToGlobalNodeOperation;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.FragmentShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.VertexShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.ShaderNodeParameter;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of output global parameters.
 *
 * @author JavaSaBr
 */
public class OutputGlobalShaderNodeElement extends GlobalShaderNodeElement {

    public OutputGlobalShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderGenerationInfo object) {
        super(container, object);
    }

    @Override
    protected @NotNull String getTitleText() {
        return "Global outputs";
    }

    @FXThread
    @Override
    public @Nullable ShaderNodeParameter parameterFor(final @NotNull ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {
        if (!fromOutputMapping) return null;
        return super.parameterFor(variable, fromOutputMapping, input);
    }

    @FXThread
    @Override
    protected void fillParameters(@NotNull final VBox container) {
        super.fillParameters(container);

        final ShaderGenerationInfo info = getObject();
        final ShaderNodeVariable vertexGlobal = info.getVertexGlobal();
        final List<ShaderNodeVariable> fragmentGlobals = info.getFragmentGlobals();

        FXUtils.addToPane(new InputShaderNodeParameter(this, vertexGlobal), container);

        for (final ShaderNodeVariable fragmentGlobal : fragmentGlobals) {
            FXUtils.addToPane(new InputShaderNodeParameter(this, fragmentGlobal), container);
        }
    }

    @Override
    public boolean canAttach(@NotNull final InputShaderNodeParameter inputParameter,
                             @NotNull final OutputShaderNodeParameter outputParameter) {

        if (!super.canAttach(inputParameter, outputParameter)) {
            return false;
        }

        final ShaderNodeVariable vertexGlobal = getObject().getVertexGlobal();
        boolean isVertex = vertexGlobal == inputParameter.getVariable();

        final ShaderNodeElement<?> sourceElement = outputParameter.getNodeElement();

        if (isVertex) {
            return sourceElement instanceof VertexShaderNodeElement;
        } else {
            return sourceElement instanceof FragmentShaderNodeElement;
        }
    }

    @Override
    public void attach(@NotNull final InputShaderNodeParameter inputParameter,
                       @NotNull final OutputShaderNodeParameter outputParameter) {
        super.attach(inputParameter, outputParameter);

        final ShaderNodeElement<?> nodeElement = outputParameter.getNodeElement();
        if (!(nodeElement instanceof MainShaderNodeElement)) {
            return;
        }

        final ShaderNode shaderNode = ((MainShaderNodeElement) nodeElement).getObject();
        final ShaderNodeVariable inVar = inputParameter.getVariable();

        final VariableMapping currentMapping = findOutMappingByNNLeftVar(shaderNode, inVar);
        final VariableMapping newMapping = makeMapping(inputParameter, outputParameter);
        newMapping.setRightSwizzling(calculateRightSwizzling(inVar, outputParameter.getVariable()));

        if (newMapping.equals(currentMapping)) {
            return;
        }

        final ShaderNodesContainer container = getContainer();
        final List<ShaderNode> currentNodes = container.findWithLeftOutputVar(inVar);
        currentNodes.remove(shaderNode);

        final ShaderNodesChangeConsumer changeConsumer = container.getChangeConsumer();
        changeConsumer.execute(new AttachVarToGlobalNodeOperation(shaderNode, newMapping, currentMapping, currentNodes));
    }
}
