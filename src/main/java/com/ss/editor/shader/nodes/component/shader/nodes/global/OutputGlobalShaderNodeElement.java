package com.ss.editor.shader.nodes.component.shader.nodes.global;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.calculateRightSwizzling;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findOutMappingByNNLeftVar;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.makeMapping;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.component.shader.nodes.operation.attach.AttachVarToGlobalNodeOperation;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.main.FragmentShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.main.MainShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.main.VertexShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.ShaderNodeParameter;
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
    @FXThread
    protected @NotNull String getTitleText() {
        return PluginMessages.NODE_ELEMENT_GLOBAL_OUTPUT;
    }

    @Override
    @FXThread
    public @Nullable ShaderNodeParameter parameterFor(final @NotNull ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {
        if (!fromOutputMapping) return null;
        return super.parameterFor(variable, fromOutputMapping, input);
    }

    @Override
    @FXThread
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
    @FXThread
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
    @FXThread
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
