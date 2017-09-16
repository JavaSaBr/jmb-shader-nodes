package com.ss.editor.shader.nodes.editor;

import com.jme3.material.Material;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.plugin.api.editor.material.BaseMaterialEditor3DState;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of the 3D part of the {@link ShaderNodesFileEditor}.
 *
 * @author JavaSaBr
 */
public class ShaderNodesEditor3DState extends BaseMaterialEditor3DState<ShaderNodesFileEditor> {

    public ShaderNodesEditor3DState(@NotNull final ShaderNodesFileEditor fileEditor) {
        super(fileEditor);
    }

    /**
     * Select a technique by the name.
     *
     * @param material the material.
     * @param name     the name.
     */
    @FromAnyThread
    public void selectTechnique(@NotNull final Material material, @NotNull final String name) {
        EXECUTOR_MANAGER.addJMETask(() -> selectTechniqueImpl(material, name));
    }

    /**
     * Select a technique by the name.
     *
     * @param material the material.
     * @param name     the name.
     */
    @JMEThread
    private void selectTechniqueImpl(@NotNull final Material material, @NotNull final String name) {
        material.selectTechnique(name, EDITOR.getRenderManager());
        updateMaterialImpl(material);
    }
}
