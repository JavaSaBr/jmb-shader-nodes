package com.ss.editor.shader.nodes.tree.action;

import static com.ss.editor.extension.property.EditablePropertyType.FILE_FROM_ASSET_FOLDER;
import static com.ss.editor.extension.property.EditablePropertyType.STRING_FROM_LIST;
import static com.ss.editor.shader.nodes.creator.ShaderNodeDefinitionsFileCreator.AVAILABLE_GLSL;
import static com.ss.editor.util.EditorUtil.toAssetPath;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.renderer.Caps;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeShaderSources;
import com.ss.editor.shader.nodes.tree.operation.AddShaderSourceOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * The action to add new shader source.
 *
 * @author JavaSaBr
 */
public class AddShaderSourceAction extends AbstractNodeAction<ChangeConsumer> {

    @NotNull
    private static final String PROP_LANGUAGE = "language";

    @NotNull
    private static final String PROP_SHADER_RESOURCE = "resource";

    public AddShaderSourceAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return PluginMessages.ACTION_ADD_SHADER_NODE_SOURCE;
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.ADD_16;
    }

    @Override
    @FXThread
    protected void process() {
        super.process();

        final TreeNode<?> node = getNode();
        final ShaderNodeShaderSources shaderSources = (ShaderNodeShaderSources) node.getElement();
        final ShaderNodeDefinition definition = shaderSources.getDefinition();
        final Shader.ShaderType type = definition.getType();

        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(STRING_FROM_LIST, PluginMessages.SND_CREATOR_LANGUAGE, PROP_LANGUAGE,
                Caps.GLSL150.name(), AVAILABLE_GLSL));
        definitions.add(new PropertyDefinition(FILE_FROM_ASSET_FOLDER, PluginMessages.SND_CREATOR_SOURCE_FILE,
                PROP_SHADER_RESOURCE, null, type.getExtension()));

        final GenericFactoryDialog dialog = new GenericFactoryDialog(definitions, this::addShaderSource, this::validate);
        dialog.setTitle(getName());
        dialog.show();
    }

    /**
     * Validate the variables.
     *
     * @param vars the vars of the definition.
     */
    @FXThread
    private boolean validate(@NotNull final VarTable vars) {

        if (!vars.has(PROP_SHADER_RESOURCE)) {
            return false;
        }

        final TreeNode<?> node = getNode();
        final ShaderNodeShaderSources shaderSources = (ShaderNodeShaderSources) node.getElement();
        final ShaderNodeDefinition definition = shaderSources.getDefinition();

        final Path shaderFile = vars.get(PROP_SHADER_RESOURCE, Path.class);
        final String shaderPath = toAssetPath(shaderFile);

        return !definition.getShadersPath().contains(shaderPath);
    }

    /**
     * Add a new shader source.
     *
     * @param vars the vars of the source.
     */
    @FXThread
    private void addShaderSource(@NotNull final VarTable vars) {

        final String language = vars.getString(PROP_LANGUAGE);
        final Path shaderFile = vars.get(PROP_SHADER_RESOURCE, Path.class);

        final TreeNode<?> node = getNode();
        final ShaderNodeShaderSources shaderSources = (ShaderNodeShaderSources) node.getElement();
        final ShaderNodeDefinition definition = shaderSources.getDefinition();
        final ShaderNodeShaderSource shaderSource = new ShaderNodeShaderSource(definition, language, toAssetPath(shaderFile));

        final ChangeConsumer changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new AddShaderSourceOperation(shaderSources, shaderSource));
    }
}
