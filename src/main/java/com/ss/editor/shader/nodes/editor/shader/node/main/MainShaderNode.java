package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.InputShaderNodeParameter;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.OutputShaderNodeParameter;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The implementation of shader element to present shader nodes.
 *
 * @author JavaSaBr
 */
public class MainShaderNode extends ShaderNodeElement<ShaderNode> {

    public MainShaderNode(@NotNull final ShaderNodesContainer container, @NotNull final ShaderNode object) {
        super(container, object);
    }

    @Override
    protected @NotNull String getTitleText() {
        return getObject().getName();
    }

    @Override
    protected void fillParameters(@NotNull final VBox container) {
        super.fillParameters(container);

        final ShaderNode shaderNode = getObject();
        final ShaderNodeDefinition definition = shaderNode.getDefinition();
        final List<ShaderNodeVariable> inputs = definition.getInputs();
        final List<ShaderNodeVariable> outputs = definition.getOutputs();

        for (final ShaderNodeVariable variable : inputs) {
            FXUtils.addToPane(new InputShaderNodeParameter(variable), container);
        }

        for (final ShaderNodeVariable variable : outputs) {
            FXUtils.addToPane(new OutputShaderNodeParameter(variable), container);
        }
    }
}
