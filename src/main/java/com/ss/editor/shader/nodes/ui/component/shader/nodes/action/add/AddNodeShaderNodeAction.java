package com.ss.editor.shader.nodes.ui.component.shader.nodes.action.add;

import static com.ss.editor.extension.property.EditablePropertyType.STRING_FROM_LIST;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findByName;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ShaderNodeDefinitionKey;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.Editor;
import com.ss.editor.FileExtensions;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.manager.ResourceManager;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.action.ShaderNodeAction;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add.AddShaderNodeOperation;
import com.ss.editor.ui.util.UIUtils;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The action to add a new shader nodes.
 *
 * @author JavaSaBr
 */
public class AddNodeShaderNodeAction extends ShaderNodeAction<TechniqueDef> {

    @NotNull
    private static final ResourceManager RESOURCE_MANAGER = ResourceManager.getInstance();

    @NotNull
    private static final Editor EDITOR = Editor.getInstance();
    public static final String PROP_DEFINITION = "definition";

    public AddNodeShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                   @NotNull final TechniqueDef techniqueDef, @NotNull final Vector2f location) {
        super(container, techniqueDef, location);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return PluginMessages.SHADER_NODE;
    }

    @Override
    @FXThread
    protected void process() {
        super.process();
        final Array<String> resources = RESOURCE_MANAGER.getAvailableResources(FileExtensions.JME_SHADER_NODE);
        UIUtils.openResourceAssetDialog(this::addNode, resources);
    }

    /**
     * Add the nodes by the resource.
     *
     * @param resource the selected resource.
     */
    @FXThread
    private void addNode(@NotNull final String resource) {

        final ShaderNodeDefinitionKey key = new ShaderNodeDefinitionKey(resource);
        key.setLoadDocumentation(false);

        final AssetManager assetManager = EDITOR.getAssetManager();
        final List<ShaderNodeDefinition> definitions = assetManager.loadAsset(key);

        if (definitions.isEmpty()) {
            return;
        }

        final ShaderNodeDefinition definition = definitions.get(0);

        if (definitions.size() <= 1) {
            addDefinition(definition);
            return;
        }

        final Array<String> definitionNames = ArrayFactory.newArray(String.class);
        definitions.forEach(element -> definitionNames.add(element.getName()));

        final Array<PropertyDefinition> properties = ArrayFactory.newArray(PropertyDefinition.class);
        properties.add(new PropertyDefinition(STRING_FROM_LIST, PluginMessages.ACTION_ADD_SHADER_NODE_DEFINITION_SELECT_DEFINITION,
                PROP_DEFINITION, definition.getName(), definitionNames));

        final GenericFactoryDialog dialog = new GenericFactoryDialog(properties, vars -> {
            final String defName = vars.getString(PROP_DEFINITION);
            definitions.stream().filter(element -> element.getName().equals(defName))
                    .findAny().ifPresent(this::addDefinition);
        });

        dialog.setTitle(PluginMessages.ACTION_ADD_SHADER_NODE_DEFINITION_SELECT_DEFINTION_TITLE);
        dialog.setButtonOkText(Messages.SIMPLE_DIALOG_BUTTON_SELECT);
        dialog.show();
    }

    /**
     * The process of adding the new definition.
     *
     * @param definition the new definition.
     */
    @FXThread
    private void addDefinition(final ShaderNodeDefinition definition) {

        final TechniqueDef techniqueDef = getObject();
        final String baseName = definition.getName();

        String resultName = baseName;

        for (int i = 1; findByName(techniqueDef, resultName) != null; i++) {
            resultName = baseName + i;
        }

        final ShaderNode shaderNode = new ShaderNode(resultName, definition, null);

        final ShaderNodesContainer container = getContainer();
        final ShaderNodesChangeConsumer changeConsumer = container.getChangeConsumer();
        changeConsumer.execute(new AddShaderNodeOperation(techniqueDef, shaderNode, getLocation()));
    }
}
