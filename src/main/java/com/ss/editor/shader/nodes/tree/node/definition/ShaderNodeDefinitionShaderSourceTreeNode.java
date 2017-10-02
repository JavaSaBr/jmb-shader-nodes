package com.ss.editor.shader.nodes.tree.node.definition;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionShaderSource;
import com.ss.editor.shader.nodes.tree.action.DeleteShaderNodeDefinitionShaderSourceAction;
import com.ss.editor.shader.nodes.ui.PluginIcons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The node to present shader source of a shader node.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionShaderSourceTreeNode extends TreeNode<ShaderNodeDefinitionShaderSource> {

    public ShaderNodeDefinitionShaderSourceTreeNode(@NotNull final ShaderNodeDefinitionShaderSource element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new DeleteShaderNodeDefinitionShaderSourceAction(nodeTree, this));
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return PluginIcons.CODE_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return "[" + getElement().getLanguage() + "] " + getElement().getShaderPath();
    }
}
