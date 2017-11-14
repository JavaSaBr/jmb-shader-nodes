package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

/**
 * The class to present shader sources of a shader node definition.
 *
 * @author JavaSaBr
 */
public class SndShaderSource {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    /**
     * The shader language.
     */
    @NotNull
    private final String language;

    /**
     * The shader path.
     */
    @NotNull
    private final String shaderPath;

    public SndShaderSource(@NotNull final ShaderNodeDefinition definition, @NotNull final String language,
                           @NotNull final String shaderPath) {
        this.definition = definition;
        this.language = language;
        this.shaderPath = shaderPath;
    }

    /**
     * Get The shader language.
     *
     * @return The shader language.
     */
    @FromAnyThread
    public @NotNull String getLanguage() {
        return language;
    }

    /**
     * Get The shader path.
     *
     * @return The shader path.
     */
    @FromAnyThread
    public @NotNull String getShaderPath() {
        return shaderPath;
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
        final SndShaderSource that = (SndShaderSource) o;
        if (!definition.equals(that.definition)) return false;
        if (!language.equals(that.language)) return false;
        return shaderPath.equals(that.shaderPath);
    }

    @Override
    public int hashCode() {
        int result = definition.hashCode();
        result = 31 * result + language.hashCode();
        result = 31 * result + shaderPath.hashCode();
        return result;
    }
}
