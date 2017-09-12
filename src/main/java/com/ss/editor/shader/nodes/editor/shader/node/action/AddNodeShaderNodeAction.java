package com.ss.editor.shader.nodes.editor.shader.node.action;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findByName;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ShaderNodeDefinitionKey;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.Editor;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.manager.ResourceManager;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.operation.add.AddShaderNodeOperation;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.ui.util.UIUtils;
import com.ss.rlib.util.array.Array;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The action to add a new shader node.
 *
 * @author JavaSaBr
 */
public class AddNodeShaderNodeAction extends ShaderNodeAction<TechniqueDef> {

    @NotNull
    private static final ResourceManager RESOURCE_MANAGER = ResourceManager.getInstance();

    @NotNull
    private static final Editor EDITOR = Editor.getInstance();

    public AddNodeShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                   @NotNull final TechniqueDef techniqueDef, @NotNull final Vector2f location) {
        super(container, techniqueDef, location);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return "Shader Node";
    }

    @Override
    @FXThread
    protected void process() {
        super.process();
        final Array<String> resources = RESOURCE_MANAGER.getAvailableResources(FileExtensions.JME_SHADER_NODE);
        UIUtils.openResourceAssetDialog(this::addNode, resources);
    }

    /**
     * Add the node by the resource.
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

        final TechniqueDef techniqueDef = getObject();
        final ShaderNodeDefinition definition = definitions.get(0);
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
