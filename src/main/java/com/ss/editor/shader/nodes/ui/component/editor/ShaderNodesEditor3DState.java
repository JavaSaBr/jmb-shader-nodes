package com.ss.editor.shader.nodes.ui.component.editor;

import com.jme3.material.Material;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.plugin.api.editor.material.BaseMaterialEditor3DPart;
import com.ss.editor.util.EditorUtil;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of the 3D part of the {@link ShaderNodesFileEditor}.
 *
 * @author JavaSaBr
 */
public class ShaderNodesEditor3DState extends BaseMaterialEditor3DPart<ShaderNodesFileEditor> {

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
        EXECUTOR_MANAGER.addJmeTask(() -> selectTechniqueImpl(material, name));
    }

    /**
     * Select a technique by the name.
     *
     * @param material the material.
     * @param name     the name.
     */
    @JmeThread
    private void selectTechniqueImpl(@NotNull final Material material, @NotNull final String name) {
        //FIXME
        material.selectTechnique(name, EditorUtil.getRenderManager());
        updateMaterialImpl(material);
    }

    @Override
    protected void handleMaterialException(@NotNull final RuntimeException exception) {
        LOGGER.warning(this, exception);
    }
}
