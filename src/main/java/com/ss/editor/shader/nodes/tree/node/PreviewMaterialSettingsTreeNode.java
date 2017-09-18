package com.ss.editor.shader.nodes.tree.node;

import com.ss.editor.model.node.material.RootMaterialSettings;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.ui.control.material.tree.node.RootMaterialSettingsTreeNode;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation fo tree node to present settings of preview material.
 *
 * @author JavaSaBr
 */
public class PreviewMaterialSettingsTreeNode extends RootMaterialSettingsTreeNode {

    public PreviewMaterialSettingsTreeNode(@NotNull final RootMaterialSettings element, final long objectId) {
        super(element, objectId);
    }

    @Override
    public @NotNull String getName() {
        return PluginMessages.TREE_NODE_PREVIEW_MATERIAL_SETTINGS;
    }
}
