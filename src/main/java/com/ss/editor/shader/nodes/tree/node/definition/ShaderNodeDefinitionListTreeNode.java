package com.ss.editor.shader.nodes.tree.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionList;
import com.ss.editor.shader.nodes.ui.PluginIcons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The node to present shader node definition list.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionListTreeNode extends TreeNode<ShaderNodeDefinitionList> {

    public ShaderNodeDefinitionListTreeNode(@NotNull final ShaderNodeDefinitionList element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return PluginIcons.LIST_16;
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return "Definitions";
    }

    @Override
    @FXThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        final ShaderNodeDefinitionList definitionList = getElement();
        final List<ShaderNodeDefinition> definitions = definitionList.getDefinitions();
        return !definitions.isEmpty();
    }

    @Override
    @FXThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final ShaderNodeDefinitionList definitionList = getElement();
        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class);

        final List<ShaderNodeDefinition> definitions = definitionList.getDefinitions();
        definitions.forEach(definition -> children.add(FACTORY_REGISTRY.createFor(definition)));

        return children;
    }
}
