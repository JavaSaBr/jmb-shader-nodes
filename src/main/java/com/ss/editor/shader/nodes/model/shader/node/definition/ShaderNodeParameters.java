package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The class to present parameters of a shader node.
 *
 * @author JavaSaBr
 */
public abstract class ShaderNodeParameters {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    protected ShaderNodeParameters(@NotNull final ShaderNodeDefinition definition) {
        this.definition = definition;
    }

    /**
     * Get the list of parameters of this shader node.
     *
     * @return the list of parameters of this shader node.
     */
    @FromAnyThread
    public abstract @NotNull List<ShaderNodeVariable> getParameters();

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
        final ShaderNodeParameters that = (ShaderNodeParameters) o;
        return definition.equals(that.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }
}
