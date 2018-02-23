package com.ss.editor.shader.nodes.ui.component.shader.nodes.global;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.OutputShaderNodeParameter;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of input global parameters.
 *
 * @author JavaSaBr
 */
public class InputGlobalShaderNodeElement extends GlobalShaderNodeElement {

    public InputGlobalShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderGenerationInfo object) {
        super(container, object);
    }

    @Override
    @FxThread
    protected @NotNull String getTitleText() {
        return PluginMessages.NODE_ELEMENT_GLOBAL_INPUT;
    }

    @Override
    @FxThread
    public @Nullable ShaderNodeParameter parameterFor(final @NotNull ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {

        if (fromOutputMapping) {
            return null;
        }

        return super.parameterFor(variable, fromOutputMapping, input);
    }

    @Override
    @FxThread
    protected void fillParameters(@NotNull final VBox container) {
        super.fillParameters(container);

        final ShaderGenerationInfo info = getObject();
        final ShaderNodeVariable vertexGlobal = info.getVertexGlobal();
        final List<ShaderNodeVariable> fragmentGlobals = info.getFragmentGlobals();

        FXUtils.addToPane(new OutputShaderNodeParameter(this, vertexGlobal), container);

        for (final ShaderNodeVariable fragmentGlobal : fragmentGlobals) {
            FXUtils.addToPane(new OutputShaderNodeParameter(this, fragmentGlobal), container);
        }
    }
}
