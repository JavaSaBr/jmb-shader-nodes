package com.ss.editor.shader.nodes.editor.shader.node.global;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.ShaderNodeElement;
import com.ss.editor.shader.nodes.editor.shader.node.main.*;
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

    @Override
    public @Nullable ShaderNodeParameter parameterFor(final @NotNull ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {
        if (!fromOutputMapping) return null;
        return super.parameterFor(variable, fromOutputMapping, input);
    }

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
            return sourceElement instanceof MaterialShaderNodeElement ||
                    sourceElement instanceof AttributeShaderNodeElement ||
                    sourceElement instanceof VertexShaderNodeElement ||
                    sourceElement instanceof WorldShaderNodeElement;
        } else {
            return sourceElement instanceof MaterialShaderNodeElement ||
                    sourceElement instanceof FragmentShaderNodeElement ||
                    sourceElement instanceof VertexShaderNodeElement ||
                    sourceElement instanceof WorldShaderNodeElement;
        }
    }
}
