package com.ss.editor.shader.nodes.ui.component.shader.nodes.main;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.ShaderNodeParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of nodes element to present shader variable.
 *
 * @author JavaSaBr
 */
public class VariableShaderNodeElement extends ShaderNodeElement<ShaderNodeVariable> {

    public VariableShaderNodeElement(@NotNull final ShaderNodesContainer container,
                                     @NotNull final ShaderNodeVariable variable) {
        super(container, variable);
    }

    @FXThread
    @Override
    public @Nullable ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {
        if (!getNameSpace().equals(variable.getNameSpace())) return null;
        return super.parameterFor(variable, fromOutputMapping, input);
    }

    /**
     * Get the namespace.
     *
     * @return the namespace.
     */
    @FXThread
    protected @NotNull String getNameSpace() {
        return "unknown";
    }
}
