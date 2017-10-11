package com.ss.editor.shader.nodes.tree.action;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.manager.ResourceManager;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImport;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImports;
import com.ss.editor.shader.nodes.tree.operation.AddShaderNodeDefinitionImportOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.ui.util.UIUtils;
import com.ss.rlib.util.array.Array;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The action to add new import.
 *
 * @author JavaSaBr
 */
public class AddShaderNodeDefinitionImportAction extends AbstractNodeAction<ChangeConsumer> {

    public AddShaderNodeDefinitionImportAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return "Add import";
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

        final ResourceManager resourceManager = ResourceManager.getInstance();
        final Array<String> libraries = resourceManager.getAvailableResources(FileExtensions.GLSL_LIB);

        UIUtils.openResourceAssetDialog(this::addImport, libraries);
    }

    /**
     * Add a new import.
     *
     * @param resource the imported resource.
     */
    @FXThread
    private void addImport(@NotNull final String resource) {

        final TreeNode<?> node = getNode();
        final ShaderNodeDefinitionImports imports = (ShaderNodeDefinitionImports) node.getElement();
        final ShaderNodeDefinition definition = imports.getDefinition();
        final ShaderNodeDefinitionImport anImport = new ShaderNodeDefinitionImport(definition, resource);

        final ChangeConsumer changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new AddShaderNodeDefinitionImportOperation(imports, anImport));
    }
}
