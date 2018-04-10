package com.ss.editor.shader.nodes.model.shader.node;

import static com.ss.rlib.util.ClassUtils.unsafeCast;
import com.jme3.export.*;
import com.jme3.material.MatParam;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.state.TechniqueDefState;
import com.ss.editor.util.EditorUtil;
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
     * The state of technique definitions.
     */
    @NotNull
    private ArrayList<TechniqueDefState> techniqueDefStates;

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
        this.techniqueDefStates = new ArrayList<>();
    }

    /**
     * Updated the states of technique definitions of this project.
     *
     * @param techniqueDefStates the states of technique definitions of this project.
     */
    @FromAnyThread
    public void updateTechniqueDefStates(@NotNull List<TechniqueDefState> techniqueDefStates) {
        this.techniqueDefStates = new ArrayList<>(techniqueDefStates);
    }

    /**
     * Get the states of technique definitions of this project.
     *
     * @return the states of technique definitions of this project.
     */
    @FromAnyThread
    public @NotNull List<TechniqueDefState> getTechniqueDefStates() {
        return techniqueDefStates;
    }

    /**
     * Set the settings list of preview material.
     *
     * @param matParams the settings list of preview material.
     */
    @FromAnyThread
    public void setMatParams(@NotNull Collection<MatParam> matParams) {
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
    public void setMaterialDefContent(@NotNull String materialDefContent) {
        this.materialDefContent = materialDefContent;
    }

    @Override
    @JmeThread
    public ShaderNodesProject jmeClone() {
        try {
            return (ShaderNodesProject) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @JmeThread
    public void cloneFields(@NotNull Cloner cloner, @NotNull Object original) {
        matParams = cloner.clone(matParams);
    }

    @Override
    @JmeThread
    public void write(@NotNull JmeExporter ex) throws IOException {

        var techStates = EditorUtil.serialize(techniqueDefStates);

        var out = ex.getCapsule(this);
        out.writeSavableArrayList(matParams, "matParams", null);
        out.write(materialDefContent, "materialDefContent", null);
        out.write(techStates, "techniqueDefStates", null);
    }

    @Override
    @JmeThread
    public void read(@NotNull JmeImporter im) throws IOException {

        var in = im.getCapsule(this);

        matParams = unsafeCast(in.readSavableArrayList("matParams", new ArrayList<>()));
        materialDefContent = in.readString("materialDefContent", null);

        var techStates = in.readByteArray("techniqueDefStates", null);

        if (techStates != null) {
            try {
                techniqueDefStates = EditorUtil.deserialize(techStates);
            } catch (RuntimeException e) {
                // we can skip it
            }
        }
    }
}
