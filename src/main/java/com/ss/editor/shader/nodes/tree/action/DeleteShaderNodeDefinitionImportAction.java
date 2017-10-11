package com.ss.editor.shader.nodes.tree.action;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImport;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImports;
import com.ss.editor.shader.nodes.tree.operation.DeleteShaderNodeDefinitionImportOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The action to delete an import.
 *
 * @author JavaSaBr
 */
public class DeleteShaderNodeDefinitionImportAction extends AbstractNodeAction<ChangeConsumer> {

    public DeleteShaderNodeDefinitionImportAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_REMOVE;
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.REMOVE_16;
    }

    @Override
    @FXThread
    protected void process() {
        super.process();

        final TreeNode<?> node = getNode();
        final TreeNode<?> parent = notNull(node.getParent());
        final ShaderNodeDefinitionImport anImport = (ShaderNodeDefinitionImport) node.getElement();
        final ShaderNodeDefinitionImports imports = (ShaderNodeDefinitionImports) parent.getElement();

        final ChangeConsumer changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new DeleteShaderNodeDefinitionImportOperation(imports, anImport));
    }
}
