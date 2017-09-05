package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of node element to present material parameters.
 *
 * @author JavaSaBr
 */
public class MaterialShaderNodeElement extends OutputVariableShaderNodeElement {

    @NotNull
    public static final String NAMESPACE = "MatParam";

    public MaterialShaderNodeElement(@NotNull final ShaderNodesContainer container,
                                     @NotNull final ShaderNodeVariable variable) {
        super(container, variable);
    }

    @Override
    protected @NotNull String getTitleText() {
        return "Material parameter";
    }

    @Override
    protected @NotNull String getNameSpace() {
        return NAMESPACE;
    }
}
