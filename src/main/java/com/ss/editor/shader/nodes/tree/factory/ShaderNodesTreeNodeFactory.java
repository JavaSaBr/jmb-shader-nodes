package com.ss.editor.shader.nodes.tree.factory;

import static com.ss.rlib.util.ClassUtils.unsafeCast;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.PreviewMaterialSettings;
import com.ss.editor.shader.nodes.model.shader.node.definition.*;
import com.ss.editor.shader.nodes.tree.node.PreviewMaterialSettingsTreeNode;
import com.ss.editor.shader.nodes.tree.node.definition.*;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.ui.control.tree.node.TreeNodeFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of tree nodes factory to create specific shader nodes in the tree.
 *
 * @author JavaSaBr
 */
public class ShaderNodesTreeNodeFactory implements TreeNodeFactory {

    private static final TreeNodeFactory INSTANCE = new ShaderNodesTreeNodeFactory();

    @FromAnyThread
    public static @NotNull TreeNodeFactory getInstance() {
        return INSTANCE;
    }

    @Override
    @FXThread
    public <T, V extends TreeNode<T>> @Nullable V createFor(@Nullable final T element, final long objectId) {

        if (element instanceof PreviewMaterialSettings) {
            return unsafeCast(new PreviewMaterialSettingsTreeNode((PreviewMaterialSettings) element, objectId));
        }

        if (element instanceof ShaderNodeDefinitionList) {
            return unsafeCast(new ShaderNodeDefinitionListTreeNode((ShaderNodeDefinitionList) element, objectId));
        } else if (element instanceof ShaderNodeDefinition) {
            return unsafeCast(new ShaderNodeDefinitionTreeNode((ShaderNodeDefinition) element, objectId));
        } else if (element instanceof ShaderNodeDefinitionParameters) {
            return unsafeCast(new ShaderNodeDefinitionParametersTreeNode((ShaderNodeDefinitionParameters) element, objectId));
        } else if (element instanceof ShaderNodeVariable) {
            return unsafeCast(new ShaderNodeDefinitionParameterTreeNode((ShaderNodeVariable) element, objectId));
        } else if (element instanceof ShaderNodeDefinitionShaderSources) {
            return unsafeCast(new ShaderNodeDefinitionShaderSourcesTreeNode((ShaderNodeDefinitionShaderSources) element, objectId));
        } else if (element instanceof ShaderNodeDefinitionShaderSource) {
            return unsafeCast(new ShaderNodeDefinitionShaderSourceTreeNode((ShaderNodeDefinitionShaderSource) element, objectId));
        }

        return null;
    }

    @Override
    @FXThread
    public int getOrder() {
        return 5;
    }
}
