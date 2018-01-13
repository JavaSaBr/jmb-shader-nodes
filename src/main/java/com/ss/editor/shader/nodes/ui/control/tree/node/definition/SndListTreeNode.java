package com.ss.editor.shader.nodes.ui.control.tree.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndList;
import com.ss.editor.shader.nodes.ui.control.tree.action.AddSndAction;
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
 * The node to present shader node definition list.
 *
 * @author JavaSaBr
 */
public class SndListTreeNode extends TreeNode<SndList> {

    public SndListTreeNode(@NotNull final SndList element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FxThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new AddSndAction(nodeTree, this));
    }

    @Override
    @FxThread
    public @Nullable Image getIcon() {
        return PluginIcons.LIST_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return PluginMessages.TREE_NODE_SHADER_NODE_DEFINITIONS;
    }

    @Override
    @FxThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        final SndList definitionList = getElement();
        final List<ShaderNodeDefinition> definitions = definitionList.getDefinitions();
        return !definitions.isEmpty();
    }

    @Override
    @FxThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final SndList definitionList = getElement();
        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class);

        final List<ShaderNodeDefinition> definitions = definitionList.getDefinitions();
        definitions.forEach(definition -> children.add(FACTORY_REGISTRY.createFor(definition)));

        return children;
    }
}
