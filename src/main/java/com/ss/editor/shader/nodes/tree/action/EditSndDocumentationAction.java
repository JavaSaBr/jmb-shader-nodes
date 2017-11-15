package com.ss.editor.shader.nodes.tree.action;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndDocumentation;
import com.ss.editor.shader.nodes.tree.node.definition.SndDocumentationTreeNode;
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

    public EditSndDocumentationAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return PluginMessages.ACTION_ADD_EDIT_DOCUMENTATION;
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.EDIT_16;
    }

    @Override
    @FXThread
    protected void process() {
        super.process();

        final SndDocumentationTreeNode node = (SndDocumentationTreeNode) getNode();
        final SndDocumentation documentation = node.getElement();
        final ShaderNodeDefinition definition = documentation.getDefinition();

        final NodeTree<ChangeConsumer> nodeTree = getNodeTree();
        final ChangeConsumer changeConsumer = notNull(nodeTree.getChangeConsumer());

        final EditSndDocumentationDialog dialog = new EditSndDocumentationDialog(changeConsumer, definition);
        dialog.show();
    }
}
