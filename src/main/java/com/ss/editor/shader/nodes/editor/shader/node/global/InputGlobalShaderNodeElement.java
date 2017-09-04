package com.ss.editor.shader.nodes.editor.shader.node.global;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.parameter.OutputShaderNodeParameter;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

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
    protected @NotNull String getTitleText() {
        return "Global inputs";
    }

    @Override
    protected void fillParameters(@NotNull final VBox container) {
        super.fillParameters(container);

        final ShaderGenerationInfo info = getObject();
        final ShaderNodeVariable vertexGlobal = info.getVertexGlobal();
        final List<ShaderNodeVariable> fragmentGlobals = info.getFragmentGlobals();

        FXUtils.addToPane(new OutputShaderNodeParameter(vertexGlobal), container);

        for (final ShaderNodeVariable fragmentGlobal : fragmentGlobals) {
            FXUtils.addToPane(new OutputShaderNodeParameter(fragmentGlobal), container);
        }
    }
}
