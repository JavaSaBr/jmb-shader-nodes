package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

/**
 * The class to present documentation of a shader node definition.
 *
 * @author JavaSaBr
 */
public class SndDocumentation {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public SndDocumentation(@NotNull ShaderNodeDefinition definition) {
        this.definition = definition;
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

    /**
     * Get the documentation of this definition.
     *
     * @return the documentation of this definition.
     */
    public @NotNull String getDocumentation() {
        return definition.getDocumentation();
    }

    /**
     * Set the documentation of this definition.
     *
     * @param documentation the documentation of this definition.
     */
    public void setDocumentation(@NotNull String documentation) {
        this.definition.setDocumentation(documentation);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (SndDocumentation) o;
        if (!definition.equals(that.definition)) return false;
        return getDocumentation().equals(that.getDocumentation());
    }

    @Override
    public int hashCode() {
        var result = definition.hashCode();
        result = 31 * result + getDocumentation().hashCode();
        return result;
    }
}
