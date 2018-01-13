package com.ss.editor.shader.nodes.ui.control.tree.node.definition;

import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSources;
import com.ss.editor.shader.nodes.ui.control.tree.action.AddSndShaderSourceAction;
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

import java.util.Map;

/**
 * The node to present shader sources of a shader node.
 *
 * @author JavaSaBr
 */
public class SndShaderSourcesTreeNode extends TreeNode<SndShaderSources> {

    public SndShaderSourcesTreeNode(@NotNull final SndShaderSources element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return PluginMessages.TREE_NODE_SHADER_NODE_SOURCES;
    }

    @Override
    @FxThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new AddSndShaderSourceAction(nodeTree, this));
    }

    @Override
    @FxThread
    public @Nullable Image getIcon() {
        return PluginIcons.CODE_16;
    }

    @Override
    @FxThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        final SndShaderSources element = getElement();
        final Map<String, String> sourceMap = element.getShadeSourceMap();
        return !sourceMap.isEmpty();
    }

    @Override
    @FxThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final SndShaderSources element = getElement();
        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class);

        final Map<String, String> sourceMap = element.getShadeSourceMap();
        sourceMap.forEach((shaderPath, language) -> children.add(FACTORY_REGISTRY.createFor(
                new SndShaderSource(element.getDefinition(), language, shaderPath))));

        return children;
    }
}
