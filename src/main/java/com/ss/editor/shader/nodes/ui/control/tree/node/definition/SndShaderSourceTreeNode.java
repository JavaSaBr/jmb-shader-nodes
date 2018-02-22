package com.ss.editor.shader.nodes.ui.control.tree.node.definition;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSource;
import com.ss.editor.shader.nodes.ui.control.tree.action.DeleteSndShaderSourceAction;
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
public class SndShaderSourceTreeNode extends TreeNode<SndShaderSource> {

    public SndShaderSourceTreeNode(@NotNull final SndShaderSource element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FxThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new DeleteSndShaderSourceAction(nodeTree, this));
    }

    @Override
    @FxThread
    public @Nullable Image getIcon() {
        return PluginIcons.CODE_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return "[" + getElement().getLanguage() + "] " + getElement().getShaderPath();
    }
}
