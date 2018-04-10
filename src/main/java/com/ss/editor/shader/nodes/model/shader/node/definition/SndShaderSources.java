package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class to present shader sources of a shader node definition.
 *
 * @author JavaSaBr
 */
public class SndShaderSources {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public SndShaderSources(@NotNull ShaderNodeDefinition definition) {
        this.definition = definition;
    }

    /**
     * Get the shader sources map where shader path - shader language.
     *
     * @return the shader sources map.
     */
    @FromAnyThread
    public @NotNull Map<String, String> getShadeSourceMap() {

        var shadersPath = definition.getShadersPath();
        var shadersLanguage = definition.getShadersLanguage();

        var result = new HashMap<String, String>();

        for (var i = 0; i < shadersPath.size(); i++) {
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
    public @NotNull List<SndShaderSource> getShadeSources() {

        var shadersPath = definition.getShadersPath();
        var shadersLanguage = definition.getShadersLanguage();

        var result = new ArrayList<SndShaderSource>();

        for (var i = 0; i < shadersPath.size(); i++) {
            result.add(new SndShaderSource(definition, shadersPath.get(i), shadersLanguage.get(i)));
        }

        return result;
    }

    /**
     * Add the shader source to sources of this definition.
     *
     * @param shaderSource the new shader source.
     */
    @FromAnyThread
    public void add(@NotNull SndShaderSource shaderSource) {

        var shadersPath = definition.getShadersPath();
        var shadersLanguage = definition.getShadersLanguage();

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
    public void add(int index, @NotNull SndShaderSource shaderSource) {

        var shadersPath = definition.getShadersPath();
        var shadersLanguage = definition.getShadersLanguage();

        shadersPath.add(index, shaderSource.getShaderPath());
        shadersLanguage.add(index, shaderSource.getLanguage());
    }

    /**
     * Remove the shader source from this definition.
     *
     * @param shaderSource the shader source.
     */
    @FromAnyThread
    public void remove(@NotNull SndShaderSource shaderSource) {

        var shadersPath = definition.getShadersPath();
        var shadersLanguage = definition.getShadersLanguage();

        var index = indexOf(shaderSource);

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
    public int indexOf(@NotNull SndShaderSource shaderSource) {

        var shadersPath = definition.getShadersPath();
        var shadersLanguage = definition.getShadersLanguage();

        for (var i = 0; i < shadersPath.size(); i++) {

            var path = shadersPath.get(i);
            var language = shadersLanguage.get(i);

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
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (SndShaderSources) o;
        return definition.equals(that.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }
}
