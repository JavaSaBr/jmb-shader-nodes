package com.ss.editor.shader.nodes.ui.control.tree.action;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ui.control.tree.node.definition.SndDocumentationTreeNode;
import com.ss.editor.shader.nodes.ui.dialog.EditSndDocumentationDialog;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The action to edit documentation of a shader node definition.
 *
 * @author JavaSaBr
 */
public class EditSndDocumentationAction extends AbstractNodeAction<ChangeConsumer> {

    public EditSndDocumentationAction(@NotNull NodeTree<?> nodeTree, @NotNull TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return PluginMessages.ACTION_ADD_EDIT_DOCUMENTATION;
    }

    @Override
    @FxThread
    protected @Nullable Image getIcon() {
        return Icons.EDIT_16;
    }

    @Override
    @FxThread
    protected void process() {
        super.process();

        var node = (SndDocumentationTreeNode) getNode();
        var documentation = node.getElement();
        var definition = documentation.getDefinition();

        var nodeTree = getNodeTree();
        var changeConsumer = notNull(nodeTree.getChangeConsumer());

        var dialog = new EditSndDocumentationDialog(changeConsumer, definition);
        dialog.show();
    }
}
