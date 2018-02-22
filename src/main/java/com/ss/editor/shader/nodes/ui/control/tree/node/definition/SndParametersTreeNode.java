package com.ss.editor.shader.nodes.ui.control.tree.node.definition;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndParameters;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndInputParameters;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndOutputParameters;
import com.ss.editor.shader.nodes.ui.control.tree.action.AddSndInputParameterAction;
import com.ss.editor.shader.nodes.ui.control.tree.action.AddSndOutputParameterAction;
import com.ss.editor.shader.nodes.ui.PluginIcons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The node to present shader node definition parameters.
 *
 * @author JavaSaBr
 */
public class SndParametersTreeNode extends TreeNode<SndParameters> {

    public SndParametersTreeNode(@NotNull final SndParameters element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return getElement() instanceof SndInputParameters ?
                PluginMessages.TREE_NODE_SHADER_NODE_INPUT_PARAMETERS : PluginMessages.TREE_NODE_SHADER_NODE_OUTPUT_PARAMETERS;
    }

    @Override
    @FxThread
    public @Nullable Image getIcon() {
        return getElement() instanceof SndInputParameters ? PluginIcons.ARROW_RIGHT_16 :
                PluginIcons.ARROW_LEFT_16;
    }

    @Override
    @FxThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);

        final SndParameters element = getElement();
        if (element instanceof SndInputParameters) {
            items.add(new AddSndInputParameterAction(nodeTree, this));
        } else if (element instanceof SndOutputParameters) {
            items.add(new AddSndOutputParameterAction(nodeTree, this));
        }
    }

    @Override
    @FxThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        final SndParameters element = getElement();
        final List<ShaderNodeVariable> parameters = element.getParameters();
        return !parameters.isEmpty();
    }

    @Override
    @FxThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final SndParameters element = getElement();
        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class);

        final List<ShaderNodeVariable> parameters = element.getParameters();
        parameters.forEach(variable -> children.add(FACTORY_REGISTRY.createFor(variable)));

        return children;
    }
}
