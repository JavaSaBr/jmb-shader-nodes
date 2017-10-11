package com.ss.editor.shader.nodes.tree.node.definition;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImport;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImports;
import com.ss.editor.shader.nodes.tree.action.AddShaderNodeDefinitionImportAction;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The node to present imports of a shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionImportsTreeNode extends TreeNode<ShaderNodeDefinitionImports> {

    public ShaderNodeDefinitionImportsTreeNode(@NotNull final ShaderNodeDefinitionImports element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return "Imports";
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return Icons.INFLUENCER_16;
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new AddShaderNodeDefinitionImportAction(nodeTree, this));
    }

    @Override
    @FXThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        final ShaderNodeDefinitionImports element = getElement();
        final List<ShaderNodeDefinitionImport> imports = element.getImports();
        return !imports.isEmpty();
    }

    @Override
    @FXThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final ShaderNodeDefinitionImports element = getElement();
        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class);

        final List<ShaderNodeDefinitionImport> imports = element.getImports();
        imports.forEach(variable -> children.add(FACTORY_REGISTRY.createFor(variable)));

        return children;
    }
}
