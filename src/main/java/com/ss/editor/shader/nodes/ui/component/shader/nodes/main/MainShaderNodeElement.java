package com.ss.editor.shader.nodes.ui.component.shader.nodes.main;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.*;

import com.jme3.math.Vector2f;
import com.jme3.shader.*;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.remove.RemoveShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.global.InputGlobalShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachAttributeToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachGlobalToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachUniformToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach.AttachVarToShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.tooltip.SndDocumentationTooltip;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The implementation of shader element to present shader nodes.
 *
 * @author JavaSaBr
 */
public class MainShaderNodeElement extends ShaderNodeElement<ShaderNode> {

    public MainShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderNode object) {
        super(container, object);
    }

    @Override
    @FxThread
    protected @NotNull String getTitleText() {
        return getObject().getDefinition().getName();
    }

    @Override
    @FxThread
    protected @NotNull Optional<Tooltip> createTooltip() {
        return Optional.of(new SndDocumentationTooltip(getObject().getDefinition()));
    }

    @FxThread
    @Override
    public @Nullable ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping,
                                                      final boolean input) {

        var shaderNode = getObject();
        if (!shaderNode.getName().equals(variable.getNameSpace())) {
            return null;
        }

        return super.parameterFor(variable, fromOutputMapping, input);
    }

    @FxThread
    @Override
    protected void fillParameters(@NotNull final VBox container) {
        super.fillParameters(container);

        var shaderNode = getObject();
        var definition = shaderNode.getDefinition();
        var inputs = definition.getInputs();
        var outputs = definition.getOutputs();

        for (var variable : inputs) {
            FXUtils.addToPane(new InputShaderNodeParameter(this, variable), container);
        }

        for (var variable : outputs) {
            FXUtils.addToPane(new OutputShaderNodeParameter(this, variable), container);
        }
    }

    @Override
    public boolean canAttach(@NotNull final InputShaderNodeParameter inputParameter,
                             @NotNull final OutputShaderNodeParameter outputParameter) {

        return !inputParameter.isUsedExpression() &&
            super.canAttach(inputParameter, outputParameter);
    }

    @Override
    @FxThread
    public void attach(@NotNull final InputShaderNodeParameter inputParameter,
                       @NotNull final OutputShaderNodeParameter outputParameter) {

        super.attach(inputParameter, outputParameter);

        var nodeElement = outputParameter.getNodeElement();
        var inVar = inputParameter.getVariable();
        var outVar = outputParameter.getVariable();
        var shaderNode = getObject();
        var currentMapping = findInMappingByNLeftVar(shaderNode, inVar);

        var newMapping = makeMapping(inputParameter, outputParameter);
        newMapping.setRightSwizzling(calculateRightSwizzling(inVar, outVar));

        if (StringUtils.isEmpty(newMapping.getRightSwizzling())) {
            newMapping.setLeftSwizzling(calculateLeftSwizzling(inVar, outputParameter.getVariable()));
        }

        if (newMapping.equals(currentMapping)) {
            return;
        }

        final var changeConsumer = getContainer().getChangeConsumer();

        if (nodeElement instanceof InputGlobalShaderNodeElement) {
            changeConsumer.execute(new AttachGlobalToShaderNodeOperation(shaderNode, newMapping, currentMapping));
        } else if (nodeElement instanceof AttributeShaderNodeElement) {
            changeConsumer.execute(new AttachAttributeToShaderNodeOperation(shaderNode, newMapping, currentMapping));
            return;
        }

        var container = nodeElement.getContainer();
        var techniqueDef = container.getTechniqueDef();

        if (nodeElement instanceof MainShaderNodeElement) {

            var outShaderNode = ((MainShaderNodeElement) nodeElement).getObject();

            changeConsumer.execute(new AttachVarToShaderNodeOperation(shaderNode, newMapping,
                currentMapping, techniqueDef, outShaderNode));

        } else if (nodeElement instanceof MaterialShaderNodeElement || nodeElement instanceof WorldShaderNodeElement) {

            var type = shaderNode.getDefinition().getType();

            if (type == Shader.ShaderType.Vertex) {

                var fragmentNodes = container.findWithRightInputVar(newMapping.getLeftVariable(),
                    FragmentShaderNodeElement.class);

                newMapping.getLeftVariable()
                    .setShaderOutput(!fragmentNodes.isEmpty());
            }

            changeConsumer.execute(new AttachUniformToShaderNodeOperation(shaderNode, outVar, techniqueDef,
                    newMapping, currentMapping));
        }
    }

    @Override
    @FxThread
    public @Nullable ShaderNodeAction<?> getDeleteAction() {
        return new RemoveShaderNodeAction(getContainer(), getObject(),
                new Vector2f((float) getLayoutX(), (float) getLayoutY()));
    }
}
