package com.ss.editor.shader.nodes.editor.shader.node.main;

import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.shader.node.action.RemoveAttributeShaderNodeAction;
import com.ss.editor.shader.nodes.editor.shader.node.action.ShaderNodeAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of node element to present attribute parameters.
 *
 * @author JavaSaBr
 */
public class AttributeShaderNodeElement extends OutputVariableShaderNodeElement {

    @NotNull
    public static final String NAMESPACE = "Attr";

    public AttributeShaderNodeElement(@NotNull final ShaderNodesContainer container,
                                      @NotNull final ShaderNodeVariable variable) {
        super(container, variable);
    }

    @Override
    protected @NotNull String getTitleText() {
        return "Attribute";
    }

    @Override
    protected @NotNull String getNameSpace() {
        return NAMESPACE;
    }

    @Override
    @FXThread
    public @Nullable ShaderNodeAction<?> getDeleteAction() {
        return new RemoveAttributeShaderNodeAction(getContainer(), getObject(),
                new Vector2f((float) getLayoutX(), (float) getLayoutY()));
    }
}
