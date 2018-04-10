package com.ss.editor.shader.nodes.model;

import com.jme3.material.Material;
import com.ss.editor.model.node.material.RootMaterialSettings;
import org.jetbrains.annotations.NotNull;

/**
 * The settings of preview material.
 *
 * @author JavaSaBr
 */
public class PreviewMaterialSettings extends RootMaterialSettings {

    public PreviewMaterialSettings(@NotNull Material material) {
        super(material);
    }
}
