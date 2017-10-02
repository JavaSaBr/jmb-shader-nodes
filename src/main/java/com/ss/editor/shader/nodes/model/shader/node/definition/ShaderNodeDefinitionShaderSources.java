package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class to present shader sources of a shader node.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionShaderSources {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public ShaderNodeDefinitionShaderSources(@NotNull final ShaderNodeDefinition definition) {
        this.definition = definition;
    }

    /**
     * Get the shader sources map where shader path - shader language.
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
     * Get the list of shader sources.
     *
     * @return the list of shader sources.
     */
    @FromAnyThread
    public @NotNull List<ShaderNodeDefinitionShaderSource> getShadeSources() {

        final List<String> shadersPath = definition.getShadersPath();
        final List<String> shadersLanguage = definition.getShadersLanguage();

        final List<ShaderNodeDefinitionShaderSource> result = new ArrayList<>();

        for (int i = 0; i < shadersPath.size(); i++) {
            result.add(new ShaderNodeDefinitionShaderSource(definition, shadersPath.get(i), shadersLanguage.get(i)));
        }

        return result;
    }

    /**
     * Add the shader source to sources of this definition.
     *
     * @param shaderSource the new shader source.
     */
    @FromAnyThread
    public void add(@NotNull final ShaderNodeDefinitionShaderSource shaderSource) {

        final List<String> shadersPath = definition.getShadersPath();
        final List<String> shadersLanguage = definition.getShadersLanguage();

        shadersPath.add(shaderSource.getShaderPath());
        shadersLanguage.add(shaderSource.getLanguage());
    }

    /**
     * Add the shader source to sources of this definition.
     *
     * @param index        the position of the shader source.
     * @param shaderSource the new shader source.
     */
    @FromAnyThread
    public void add(final int index, @NotNull final ShaderNodeDefinitionShaderSource shaderSource) {

        final List<String> shadersPath = definition.getShadersPath();
        final List<String> shadersLanguage = definition.getShadersLanguage();

        shadersPath.add(index, shaderSource.getShaderPath());
        shadersLanguage.add(index, shaderSource.getLanguage());
    }

    /**
     * Remove the shader source from this definition.
     *
     * @param shaderSource the shader source.
     */
    @FromAnyThread
    public void remove(@NotNull final ShaderNodeDefinitionShaderSource shaderSource) {

        final List<String> shadersPath = definition.getShadersPath();
        final List<String> shadersLanguage = definition.getShadersLanguage();

        final int index = indexOf(shaderSource);

        if (index == -1) {
            throw new IllegalArgumentException("not found the shader source " + shaderSource);
        }

        shadersPath.remove(index);
        shadersLanguage.remove(index);
    }

    /**
     * Get the position of the shader source in this definition.
     *
     * @param shaderSource the shader source.
     * @return the position or -1.
     */
    @FromAnyThread
    public int indexOf(@NotNull final ShaderNodeDefinitionShaderSource shaderSource) {

        final List<String> shadersPath = definition.getShadersPath();
        final List<String> shadersLanguage = definition.getShadersLanguage();

        for (int i = 0; i < shadersPath.size(); i++) {

            final String path = shadersPath.get(i);
            final String language = shadersLanguage.get(i);

            if (path.equals(shaderSource.getShaderPath()) && language.equals(shaderSource.getLanguage())) {
                return i;
            }
        }

        return -1;
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
        final ShaderNodeDefinitionShaderSources that = (ShaderNodeDefinitionShaderSources) o;
        return definition.equals(that.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }
}
