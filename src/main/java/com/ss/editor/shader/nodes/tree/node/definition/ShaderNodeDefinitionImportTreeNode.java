package com.ss.editor.shader.nodes.tree.node.definition;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImport;
import com.ss.editor.shader.nodes.tree.action.DeleteShaderNodeDefinitionImportAction;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The node to present an import of a shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionImportTreeNode extends TreeNode<ShaderNodeDefinitionImport> {

    public ShaderNodeDefinitionImportTreeNode(@NotNull final ShaderNodeDefinitionImport element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new DeleteShaderNodeDefinitionImportAction(nodeTree, this));
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return Icons.ATOM_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return getElement().getPath();
    }
}
