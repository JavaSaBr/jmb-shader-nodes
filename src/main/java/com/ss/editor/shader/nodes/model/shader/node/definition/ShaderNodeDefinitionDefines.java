package com.ss.editor.shader.nodes.model.shader.node.definition;

import static java.util.stream.Collectors.toList;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The class to present defines of a shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionDefines {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public ShaderNodeDefinitionDefines(@NotNull final ShaderNodeDefinition definition) {
        this.definition = definition;
    }

    /**
     * Get the list of defines of this definition.
     *
     * @return the list of defines of this definition.
     */
    @FromAnyThread
    public @NotNull List<ShaderNodeDefinitionDefine> getDefines() {
        return definition.getDefines().stream()
                .map(name -> new ShaderNodeDefinitionDefine(getDefinition(), name))
                .collect(toList());
    }

    /**
     * Add the new define to the shader node definition.
     *
     * @param define the new define.
     */
    @FromAnyThread
    public void add(@NotNull final ShaderNodeDefinitionDefine define) {
        definition.getDefines().add(define.getDefine());
    }

    /**
     * Add the new define to the shader node definition.
     *
     * @param index  the index.
     * @param define the new define.
     */
    @FromAnyThread
    public void add(final int index, @NotNull final ShaderNodeDefinitionDefine define) {
        definition.getDefines().add(index, define.getDefine());
    }

    /**
     * Get the index of position of the define.
     *
     * @param define the define.
     * @return the index of the define or -1.
     */
    @FromAnyThread
    public int indexOf(@NotNull final ShaderNodeDefinitionDefine define) {
        return definition.getDefines().indexOf(define.getDefine());
    }

    /**
     * Remove the old define from the shader node definition.
     *
     * @param define the old define.
     */
    @FromAnyThread
    public void remove(@NotNull final ShaderNodeDefinitionDefine define) {
        definition.getDefines().remove(define.getDefine());
    }

    /**
     * Rename the define.
     *
     * @param define  the define.
     * @param newName the new name.
     */
    @FromAnyThread
    public void rename(@NotNull final ShaderNodeDefinitionDefine define, @NotNull final String newName) {
        final List<String> defines = definition.getDefines();
        final int position = defines.indexOf(define.getDefine());
        defines.remove(position);
        defines.add(position, newName);
        define.setDefine(newName);
    }

    /**
     * Get the definition.
     *
     * @return the definition.
     */
    @FromAnyThread
    public @NotNull ShaderNodeDefinition getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ShaderNodeDefinitionDefines that = (ShaderNodeDefinitionDefines) o;
        return definition.equals(that.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }
}
