package com.ss.editor.shader.nodes.component.shader.nodes.global;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.nodes.parameter.ShaderNodeParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of a global parameters.
 *
 * @author JavaSaBr
 */
public class GlobalShaderNodeElement extends ShaderNodeElement<ShaderGenerationInfo> {

    @NotNull
    public static final String NAMESPACE = "Global";

    public GlobalShaderNodeElement(@NotNull final ShaderNodesContainer container, @NotNull final ShaderGenerationInfo object) {
        super(container, object);
    }

    @Override
    @FXThread
    public @Nullable ShaderNodeParameter parameterFor(@NotNull final ShaderNodeVariable variable,
                                                      final boolean fromOutputMapping, final boolean input) {
        if (!NAMESPACE.equals(variable.getNameSpace())) return null;
        return super.parameterFor(variable, fromOutputMapping, input);
    }
}
