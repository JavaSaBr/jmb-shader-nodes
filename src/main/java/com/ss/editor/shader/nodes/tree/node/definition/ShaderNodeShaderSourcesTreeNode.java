package com.ss.editor.shader.nodes.tree.node.definition;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeShaderSource;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeShaderSources;
import com.ss.editor.shader.nodes.tree.action.AddShaderSourceAction;
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
public class ShaderNodeShaderSourcesTreeNode extends TreeNode<ShaderNodeShaderSources> {

    public ShaderNodeShaderSourcesTreeNode(@NotNull final ShaderNodeShaderSources element, final long objectId) {
        super(element, objectId);
    }

    @Override
    @FromAnyThread
    public @NotNull String getName() {
        return "Shader sources";
    }

    @Override
    @FXThread
    public void fillContextMenu(@NotNull final NodeTree<?> nodeTree, @NotNull final ObservableList<MenuItem> items) {
        super.fillContextMenu(nodeTree, items);
        items.add(new AddShaderSourceAction(nodeTree, this));
    }

    @Override
    @FXThread
    public @Nullable Image getIcon() {
        return PluginIcons.CODE_16;
    }

    @Override
    @FXThread
    public boolean hasChildren(@NotNull final NodeTree<?> nodeTree) {
        final ShaderNodeShaderSources element = getElement();
        final Map<String, String> sourceMap = element.getShadeSourceMap();
        return !sourceMap.isEmpty();
    }

    @Override
    @FXThread
    public @NotNull Array<TreeNode<?>> getChildren(@NotNull final NodeTree<?> nodeTree) {

        final ShaderNodeShaderSources element = getElement();
        final Array<TreeNode<?>> children = ArrayFactory.newArray(TreeNode.class);

        final Map<String, String> sourceMap = element.getShadeSourceMap();
        sourceMap.forEach((shaderPath, language) -> children.add(FACTORY_REGISTRY.createFor(
                new ShaderNodeShaderSource(element.getDefinition(), language, shaderPath))));

        return children;
    }
}
