package com.ss.editor.shader.nodes.tree.node.definition;

import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeInputParameters;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeOutputParameters;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeParameters;
import com.ss.editor.shader.nodes.tree.action.AddInputParameterAction;
import com.ss.editor.shader.nodes.tree.action.AddOutputParameterAction;
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
 * The node to present shader node parameters.
 *
 * @author JavaSaBr
 */
public class ShaderNodeParametersTreeNode extends TreeNode<ShaderNodeParameters> {

    public ShaderNodeParametersTreeNode(@NotNull final ShaderNodeParameters element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return getElement() instanceof ShaderNodeInputParameters ?
                PluginMessages.TREE_NODE_SHADER_NODE_INPUT_PARAMETERS : PluginMessages.TREE_NODE_SHADER_NODE_OUTPUT_PARAMETERS;
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return getElement() instanceof ShaderNodeInputParameters ? PluginIcons.ARROW_RIGHT_16 :
                PluginIcons.ARROW_LEFT_16;
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);

        final ShaderNodeParameters element = getElement();
        if (element instanceof ShaderNodeInputParameters) {
            items.add(new AddInputParameterAction(nodeTree, this));
        } else if (element instanceof ShaderNodeOutputParameters) {
            items.add(new AddOutputParameterAction(nodeTree, this));
        }
    }

    @Override
    @FXThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        final ShaderNodeParameters element = getElement();
        final List<ShaderNodeVariable> parameters = element.getParameters();
        return !parameters.isEmpty();
    }

    @Override
    @FXThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final ShaderNodeParameters element = getElement();
        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class);

        final List<ShaderNodeVariable> parameters = element.getParameters();
        parameters.forEach(variable -> children.add(FACTORY_REGISTRY.createFor(variable)));

        return children;
    }
}
