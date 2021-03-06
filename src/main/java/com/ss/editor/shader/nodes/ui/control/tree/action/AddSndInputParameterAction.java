package com.ss.editor.shader.nodes.ui.control.tree.action;

import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The action to add new input parameter.
 *
 * @author JavaSaBr
 */
public class AddSndInputParameterAction extends AddSndParameterAction {

    public AddSndInputParameterAction(@NotNull final NodeTree<?> nodeTree,
                                      @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @NotNull List<ShaderNodeVariable> getCurrentParameters(@NotNull final ShaderNodeDefinition definition) {
        return definition.getInputs();
    }

    @Override
    @FXThread
    protected @NotNull List<ShaderNodeVariable> getOppositeParameters(@NotNull final ShaderNodeDefinition definition) {
        return definition.getOutputs();
    }
}
