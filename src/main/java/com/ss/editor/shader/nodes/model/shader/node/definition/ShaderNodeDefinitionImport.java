package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

/**
 * The class to present an import of a shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionImport {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    /**
     * The path of the imported shader.
     */
    @NotNull
    private String path;

    public ShaderNodeDefinitionImport(@NotNull final ShaderNodeDefinition definition, @NotNull final String path) {
        this.definition = definition;
        this.path = path;
    }

    /**
     * Get the path of the imported shader.
     *
     * @return the path of the imported shader.
     */
    @FromAnyThread
    public @NotNull String getPath() {
        return path;
    }

    /**
     * Set the path of the imported shader.
     *
     * @param path the path of the imported shader.
     */
    @FromAnyThread
    public void setPath(@NotNull final String path) {
        this.path = path;
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
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final ShaderNodeDefinitionImport that = (ShaderNodeDefinitionImport) obj;
        if (!definition.equals(that.definition)) return false;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        int result = definition.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }
}
