package com.ss.editor.shader.nodes;

import static com.ss.editor.plugin.api.messages.MessagesPluginFactory.getResourceBundle;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * The class with localised all plugin messages.
 *
 * @author JavaSaBr
 */
public interface PluginMessages {

    @NotNull ResourceBundle RESOURCE_BUNDLE = getResourceBundle(ShaderNodesEditorPlugin.class,
            "com/ss/editor/shader/nodes/messages/messages");

    @NotNull String SN_CREATOR_TITLE = RESOURCE_BUNDLE.getString("SNCreatorTitle");
    @NotNull String SN_CREATOR_DESCRIPTION = RESOURCE_BUNDLE.getString("SNCreatorDescription");

    @NotNull String PROPERTY_DEFAULT = RESOURCE_BUNDLE.getString("PropertyDefault");

    @NotNull String VERTEX_ATTRIBUTE = RESOURCE_BUNDLE.getString("VertexAttribute");
    @NotNull String MATERIAL_PARAMETER = RESOURCE_BUNDLE.getString("MaterialParameter");
    @NotNull String MATERIAL_TEXTURE = RESOURCE_BUNDLE.getString("MaterialTexture");
    @NotNull String WORLD_PARAMETER = RESOURCE_BUNDLE.getString("WorldParameter");
    @NotNull String SHADER_NODE = RESOURCE_BUNDLE.getString("ShaderNode");
    @NotNull String COLOR_SPACE = RESOURCE_BUNDLE.getString("ColorSpace");

    @NotNull String ACTION_ADD_VERTEX_ATTRIBUTE_TITLE = RESOURCE_BUNDLE.getString("ActionAddVertexAttributeTitle");
    @NotNull String ACTION_ADD_WORLD_PARAMETER_TITLE = RESOURCE_BUNDLE.getString("ActionAddWorldParameterTitle");
    @NotNull String ACTION_ADD_MATERIAL_PARAMETER_TITLE = RESOURCE_BUNDLE.getString("ActionAddMaterialParameterTitle");

    @NotNull String ACTION_DELETE = RESOURCE_BUNDLE.getString("ActionDelete");
}
