package com.ss.editor.shader.nodes.tree.node.definition;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeParameters;
import com.ss.editor.shader.nodes.tree.action.DeleteParameterAction;
import com.ss.editor.shader.nodes.tree.operation.RenameParameterOperation;
import com.ss.editor.shader.nodes.ui.PluginIcons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.StringUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The node to present shader node parameter.
 *
 * @author JavaSaBr
 */
public class ShaderNodeParameterTreeNode extends TreeNode<ShaderNodeVariable> {

    public ShaderNodeParameterTreeNode(@NotNull final ShaderNodeVariable element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new DeleteParameterAction(nodeTree, this));
    }

    @FXThread
    @Override
    public void changeName(@NotNull final NodeTree<?> nodeTree, @NotNull final String newName) {
        if (StringUtils.equals(getName(), newName)) return;

        super.changeName(nodeTree, newName);

        final TreeNode<?> parent = notNull(getParent());
        final ShaderNodeParameters parameters = (ShaderNodeParameters) parent.getElement();
        final ShaderNodeVariable variable = getElement();

        final ChangeConsumer changeConsumer = notNull(nodeTree.getChangeConsumer());
        changeConsumer.execute(new RenameParameterOperation(variable.getName(), newName, parameters, variable));
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return PluginIcons.VARIABLE_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return getElement().getName();
    }

    @Override
    @FXThread
    public boolean canEditName() {
        return true;
    }
}
