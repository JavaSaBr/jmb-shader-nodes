package com.ss.editor.shader.nodes.ui.control.tree.action;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSources;
import com.ss.editor.shader.nodes.ui.control.tree.operation.DeleteSndShaderSourceOperation;
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
public class DeleteSndShaderSourceAction extends AbstractNodeAction<ChangeConsumer> {

    public DeleteSndShaderSourceAction(@NotNull NodeTree<?> nodeTree, @NotNull TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return Messages.MODEL_NODE_TREE_ACTION_REMOVE;
    }

    @Override
    @FxThread
    protected @Nullable Image getIcon() {
        return Icons.REMOVE_16;
    }

    @Override
    @FxThread
    protected void process() {
        super.process();

        var node = getNode();
        var parent = notNull(node.getParent());
        var shaderSource = (SndShaderSource) node.getElement();
        var shaderSources = (SndShaderSources) parent.getElement();

        var changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new DeleteSndShaderSourceOperation(shaderSources, shaderSource));
    }
}
