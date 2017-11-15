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

    @NotNull String SNS_CREATOR_TITLE = RESOURCE_BUNDLE.getString("SNSCreatorTitle");
    @NotNull String SNS_CREATOR_DESCRIPTION = RESOURCE_BUNDLE.getString("SNSCreatorDescription");
    @NotNull String SNS_EDITOR_NAME = RESOURCE_BUNDLE.getString("SNSEditorName");

    @NotNull String SND_CREATOR_TITLE = RESOURCE_BUNDLE.getString("SNDCreatorTitle");
    @NotNull String SND_CREATOR_DESCRIPTION = RESOURCE_BUNDLE.getString("SNDCreatorDescription");
    @NotNull String SND_CREATOR_DEFINITION_NAME = RESOURCE_BUNDLE.getString("SNDCreatorDefinitionName");
    @NotNull String SND_CREATOR_LANGUAGE = RESOURCE_BUNDLE.getString("SNDCreatorLanguage");
    @NotNull String SND_CREATOR_SOURCE_FILE = RESOURCE_BUNDLE.getString("SNDCreatorSourceFile");
    @NotNull String SND_EDITOR_NAME = RESOURCE_BUNDLE.getString("SNDEditorName");
    @NotNull String SND_EDITOR_TOOL_STRUCTURE = RESOURCE_BUNDLE.getString("SNDEditorToolStructure");
    @NotNull String SND_EDITOR_DOCUMENT_DIALOG = RESOURCE_BUNDLE.getString("SNDEditorDocumentDialog");

    @NotNull String SNS_EDITOR_TOOL_FRAGMENT = RESOURCE_BUNDLE.getString("SNSEditorToolFragment");
    @NotNull String SNS_EDITOR_TOOL_VERTEX = RESOURCE_BUNDLE.getString("SNSEditorToolVertex");
    @NotNull String SNS_EDITOR_TOOL_MD = RESOURCE_BUNDLE.getString("SNSEditorToolMD");
    @NotNull String SNS_EDITOR_ACTION_EXPORT = RESOURCE_BUNDLE.getString("SNSEditorActionExport");
    @NotNull String SNS_EDITOR_ACTION_IMPORT = RESOURCE_BUNDLE.getString("SNSEditorActionImport");
    @NotNull String SNS_EDITOR_ACTION_ADD_TECHNIQUE = RESOURCE_BUNDLE.getString("SNSEditorActionAddTechnique");
    @NotNull String SNS_EDITOR_LABEL_TECHNIQUE = RESOURCE_BUNDLE.getString("SNSEditorLabelTechnique");
    @NotNull String SNS_EDITOR_LABEL_INCORRECT_MD_TO_IMPORT = RESOURCE_BUNDLE.getString("SNSEditorLabelIncorrectMDToImport");

    @NotNull String VERTEX_ATTRIBUTE = RESOURCE_BUNDLE.getString("VertexAttribute");
    @NotNull String MATERIAL_PARAMETER = RESOURCE_BUNDLE.getString("MaterialParameter");
    @NotNull String MATERIAL_TEXTURE = RESOURCE_BUNDLE.getString("MaterialTexture");
    @NotNull String WORLD_PARAMETER = RESOURCE_BUNDLE.getString("WorldParameter");
    @NotNull String SHADER_NODE = RESOURCE_BUNDLE.getString("ShaderNode");

    @NotNull String ACTION_ADD_VERTEX_ATTRIBUTE_TITLE = RESOURCE_BUNDLE.getString("ActionAddVertexAttributeTitle");
    @NotNull String ACTION_ADD_WORLD_PARAMETER_TITLE = RESOURCE_BUNDLE.getString("ActionAddWorldParameterTitle");
    @NotNull String ACTION_ADD_MATERIAL_PARAMETER_TITLE = RESOURCE_BUNDLE.getString("ActionAddMaterialParameterTitle");
    @NotNull String ACTION_ADD_SND_PARAMETER = RESOURCE_BUNDLE.getString("ActionAddSNDParameter");
    @NotNull String ACTION_ADD_SHADER_NODE_DEFINITION = RESOURCE_BUNDLE.getString("ActionAddShaderNodeDefinition");
    @NotNull String ACTION_ADD_SHADER_NODE_SOURCE = RESOURCE_BUNDLE.getString("ActionAddShaderNodeSource");
    @NotNull String ACTION_ADD_EDIT_DOCUMENTATION = RESOURCE_BUNDLE.getString("ActionEditDocumentation");

    @NotNull String NODE_ELEMENT_GLOBAL_INPUT = RESOURCE_BUNDLE.getString("NodeElementGlobalInput");
    @NotNull String NODE_ELEMENT_GLOBAL_OUTPUT = RESOURCE_BUNDLE.getString("NodeElementGlobalOutput");
    @NotNull String NODE_ELEMENT_VERTEX_ATTRIBUTE = RESOURCE_BUNDLE.getString("NodeElementVertexAttribute");
    @NotNull String NODE_ELEMENT_WORLD_PARAMETER = RESOURCE_BUNDLE.getString("NodeElementWorldParameter");
    @NotNull String NODE_ELEMENT_MATERIAL_PARAMETER = RESOURCE_BUNDLE.getString("NodeElementMaterialParameter");

    @NotNull String TREE_NODE_PREVIEW_MATERIAL_SETTINGS = RESOURCE_BUNDLE.getString("TreeNodePreviewMaterialSettings");
    @NotNull String TREE_NODE_SHADER_NODE_DEFINITIONS = RESOURCE_BUNDLE.getString("TreeNodeShaderNodeDefinitions");
    @NotNull String TREE_NODE_SHADER_NODE_INPUT_PARAMETERS = RESOURCE_BUNDLE.getString("TreeNodeShaderNodeInputParameters");
    @NotNull String TREE_NODE_SHADER_NODE_OUTPUT_PARAMETERS = RESOURCE_BUNDLE.getString("TreeNodeShaderNodeOutputParameters");
    @NotNull String TREE_NODE_SHADER_NODE_SOURCES = RESOURCE_BUNDLE.getString("TreeNodeShaderNodeSources");
    @NotNull String TREE_NODE_SHADER_NODE_DOCUMENTATION = RESOURCE_BUNDLE.getString("TreeNodeShaderNodeDocumentation");

}
