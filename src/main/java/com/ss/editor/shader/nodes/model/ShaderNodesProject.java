package com.ss.editor.shader.nodes.model;

import com.jme3.export.*;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * The implementation of shader nodes project.
 *
 * @author JavaSaBr
 */
public class ShaderNodesProject implements JmeCloneable, Savable {

    /**
     * The current material definition.
     */
    @Nullable
    private MaterialDef materialDef;

    /**
     * The material to debug.
     */
    @Nullable
    private Material material;

    /**
     * Get the material to debug.
     *
     * @return the material to debug.
     */
    public @Nullable Material getMaterial() {
        return material;
    }

    /**
     * Set the material to debug.
     *
     * @param material the material to debug.
     */
    public void setMaterial(@Nullable final Material material) {
        this.material = material;
    }

    /**
     * Get the current material definition.
     *
     * @return the current material definition.
     */
    public @Nullable MaterialDef getMaterialDef() {
        return materialDef;
    }

    /**
     * Set the current material definition.
     *
     * @param materialDef the current material definition.
     */
    public void setMaterialDef(@Nullable final MaterialDef materialDef) {
        this.materialDef = materialDef;
    }

    @Override
    public ShaderNodesProject jmeClone() {
        try {
            return (ShaderNodesProject) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cloneFields(@NotNull final Cloner cloner, @NotNull final Object original) {
        material = cloner.clone(material);
        materialDef = cloner.clone(materialDef);
    }

    @Override
    public void write(@NotNull final JmeExporter ex) throws IOException {
        final OutputCapsule out = ex.getCapsule(this);
        out.write(material, "material", null);
        out.write(materialDef, "materialDef", null);
    }

    @Override
    public void read(@NotNull final JmeImporter im) throws IOException {
        final InputCapsule in = im.getCapsule(this);
        material = (Material) in.readSavable("material", null);
        materialDef = (MaterialDef) in.readSavable("materialDef", null);
    }
}
