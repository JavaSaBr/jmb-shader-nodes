package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class to present shader sources of a shader node.
 *
 * @author JavaSaBr
 */
public class ShaderNodeShaderSources {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public ShaderNodeShaderSources(@NotNull final ShaderNodeDefinition definition) {
        this.definition = definition;
    }

    /**
     * Get the shader sources map where shader path -> shader language.
     *
     * @return the shader sources map.
     */
    @FromAnyThread
    public @NotNull Map<String, String> getShadeSourceMap() {

        final List<String> shadersPath = definition.getShadersPath();
        final List<String> shadersLanguage = definition.getShadersLanguage();

        final Map<String, String> result = new HashMap<>();

        for (int i = 0; i < shadersPath.size(); i++) {
            result.put(shadersPath.get(i), shadersLanguage.get(i));
        }

        return result;
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
        final ShaderNodeShaderSources that = (ShaderNodeShaderSources) o;
        return definition.equals(that.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }
}
