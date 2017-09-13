package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.material.MatParam;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VarType;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.action.RemoveMaterialParamShaderNodeAction;
import com.ss.editor.shader.nodes.editor.shader.node.action.ShaderNodeAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of node element to present material parameters.
 *
 * @author JavaSaBr
 */
public class MaterialShaderNodeElement extends OutputVariableShaderNodeElement {

    @NotNull
    public static final String NAMESPACE = "MatParam";

    /**
     * Convert the mat param to shader node variable.
     *
     * @param matParam the mat param.
     * @return the shader node variable.
     */
    public static @NotNull ShaderNodeVariable toVariable(@NotNull final MatParam matParam) {
        final VarType type = matParam.getVarType();
        return new ShaderNodeVariable(type.getGlslType(), NAMESPACE, matParam.getName(),
                null, "m_");
    }

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

    @Override
    @FXThread
    public @Nullable ShaderNodeAction<?> getDeleteAction() {
        return new RemoveMaterialParamShaderNodeAction(getContainer(), getObject(),
                new Vector2f((float) getLayoutX(), (float) getLayoutY()));
    }
}
