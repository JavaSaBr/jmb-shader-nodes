package com.ss.editor.shader.nodes.model;

import com.jme3.export.*;
import com.jme3.material.MatParam;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The implementation of shader nodes project.
 *
 * @author JavaSaBr
 */
public class ShaderNodesProject implements JmeCloneable, Savable {

    /**
     * The settings list of preview material.
     */
    @NotNull
    private ArrayList<MatParam> matParams;

    /**
     * The content of material definition.
     */
    @Nullable
    private String materialDefContent;

    public ShaderNodesProject() {
        this.matParams = new ArrayList<>();
    }

    /**
     * Set the settings list of preview material.
     *
     * @param matParams the settings list of preview material.
     */
    @FromAnyThread
    public void setMatParams(@NotNull final Collection<MatParam> matParams) {
        this.matParams = new ArrayList<>(matParams);
    }

    /**
     * Get the settings list of preview material.
     *
     * @return the settings list of preview material.
     */
    @FromAnyThread
    public @NotNull List<MatParam> getMatParams() {
        return matParams;
    }

    /**
     * Get the content of material definition.
     *
     * @return the content of material definition.
     */
    @FromAnyThread
    public @Nullable String getMaterialDefContent() {
        return materialDefContent;
    }

    /**
     * Set the content of material definition.
     *
     * @param materialDefContent the content of material definition.
     */
    @FromAnyThread
    public void setMaterialDefContent(@NotNull final String materialDefContent) {
        this.materialDefContent = materialDefContent;
    }

    @Override
    public ShaderNodesProject jmeClone() {
        try {
            return (ShaderNodesProject) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cloneFields(@NotNull final Cloner cloner, @NotNull final Object original) {
        matParams = cloner.clone(matParams);
    }

    @Override
    public void write(@NotNull final JmeExporter ex) throws IOException {
        final OutputCapsule out = ex.getCapsule(this);
        out.writeSavableArrayList(matParams, "matParams", null);
        out.write(materialDefContent, "materialDefContent", null);
    }

    @Override
    public void read(@NotNull final JmeImporter im) throws IOException {
        final InputCapsule in = im.getCapsule(this);
        matParams = in.readSavableArrayList("matParams", new ArrayList<>());
        materialDefContent = in.readString("materialDefContent", null);
    }
}
