package com.ss.editor.shader.nodes.component.shader.nodes.main;

import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.nodes.action.remove.RemoveAttributeShaderNodeAction;
import com.ss.editor.shader.nodes.component.shader.nodes.action.ShaderNodeAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of nodes element to present attribute parameters.
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
    @FXThread
    protected @NotNull String getTitleText() {
        return PluginMessages.NODE_ELEMENT_VERTEX_ATTRIBUTE;
    }

    @Override
    @FXThread
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
