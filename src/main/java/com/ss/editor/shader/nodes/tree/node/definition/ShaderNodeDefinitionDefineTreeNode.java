package com.ss.editor.shader.nodes.tree.node.definition;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefine;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefines;
import com.ss.editor.shader.nodes.tree.action.DeleteShaderNodeDefinitionDefineAction;
import com.ss.editor.shader.nodes.tree.operation.RenameShaderNodeDefinitionDefineOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.StringUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The node to present a define of a shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionDefineTreeNode extends TreeNode<ShaderNodeDefinitionDefine> {

    public ShaderNodeDefinitionDefineTreeNode(@NotNull final ShaderNodeDefinitionDefine element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new DeleteShaderNodeDefinitionDefineAction(nodeTree, this));
    }

    @FXThread
    @Override
    public void changeName(@NotNull final NodeTree<?> nodeTree, @NotNull final String newName) {
        if (StringUtils.equals(getName(), newName)) return;

        super.changeName(nodeTree, newName);

        final TreeNode<?> parent = notNull(getParent());
        final ShaderNodeDefinitionDefines defines = (ShaderNodeDefinitionDefines) parent.getElement();
        final ShaderNodeDefinitionDefine define = getElement();

        final ChangeConsumer changeConsumer = notNull(nodeTree.getChangeConsumer());
        changeConsumer.execute(new RenameShaderNodeDefinitionDefineOperation(define.getDefine(), newName, defines, define));
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return Icons.ATOM_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return getElement().getDefine();
    }

    @Override
    @FXThread
    public boolean canEditName() {
        return true;
    }
}
