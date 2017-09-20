package com.ss.editor.shader.nodes.tree.factory;

import static com.ss.rlib.util.ClassUtils.unsafeCast;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.model.PreviewMaterialSettings;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionList;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeParameters;
import com.ss.editor.shader.nodes.tree.node.PreviewMaterialSettingsTreeNode;
import com.ss.editor.shader.nodes.tree.node.definition.ShaderNodeDefinitionListTreeNode;
import com.ss.editor.shader.nodes.tree.node.definition.ShaderNodeDefinitionTreeNode;
import com.ss.editor.shader.nodes.tree.node.definition.ShaderNodeParameterTreeNode;
import com.ss.editor.shader.nodes.tree.node.definition.ShaderNodeParametersTreeNode;
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
        } else if (element instanceof ShaderNodeParameters) {
            return unsafeCast(new ShaderNodeParametersTreeNode((ShaderNodeParameters) element, objectId));
        } else if (element instanceof ShaderNodeVariable) {
            return unsafeCast(new ShaderNodeParameterTreeNode((ShaderNodeVariable) element, objectId));
        }

        return null;
    }

    @Override
    @FXThread
    public int getOrder() {
        return 5;
    }
}
