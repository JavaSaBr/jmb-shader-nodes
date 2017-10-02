package com.ss.editor.shader.nodes.tree.action;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionShaderSources;
import com.ss.editor.shader.nodes.tree.operation.DeleteShaderNodeDefinitionShaderSourceOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The action to delete a shader source.
 *
 * @author JavaSaBr
 */
public class DeleteShaderNodeDefinitionShaderSourceAction extends AbstractNodeAction<ChangeConsumer> {

    public DeleteShaderNodeDefinitionShaderSourceAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
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
        final ShaderNodeDefinitionShaderSource shaderSource = (ShaderNodeDefinitionShaderSource) node.getElement();
        final ShaderNodeDefinitionShaderSources shaderSources = (ShaderNodeDefinitionShaderSources) parent.getElement();

        final ChangeConsumer changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new DeleteShaderNodeDefinitionShaderSourceOperation(shaderSources, shaderSource));
    }
}
