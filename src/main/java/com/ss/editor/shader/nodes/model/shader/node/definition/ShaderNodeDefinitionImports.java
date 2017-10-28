package com.ss.editor.shader.nodes.model.shader.node.definition;

import static java.util.stream.Collectors.toList;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.shader.nodes.util.ShaderNodeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The class to present imports of a shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionImports {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    public ShaderNodeDefinitionImports(@NotNull final ShaderNodeDefinition definition) {
        this.definition = definition;
    }

    /**
     * Get the list of imports of this definition.
     *
     * @return the list of imports of this definition.
     */
    @FromAnyThread
    public @NotNull List<ShaderNodeDefinitionImport> getImports() {
        return ShaderNodeUtils.getImports(definition).stream()
                .map(path -> new ShaderNodeDefinitionImport(getDefinition(), path))
                .collect(toList());
    }

    /**
     * Add the new import to the shader node definition.
     *
     * @param imp the new import.
     */
    @FromAnyThread
    public void add(@NotNull final ShaderNodeDefinitionImport imp) {
        ShaderNodeUtils.getImports(definition).add(imp.getPath());
    }

    /**
     * Add the new import to the shader node definition.
     *
     * @param index the index.
     * @param imp   the new import.
     */
    @FromAnyThread
    public void add(final int index, @NotNull final ShaderNodeDefinitionImport imp) {
        ShaderNodeUtils.getImports(definition).add(index, imp.getPath());
    }

    /**
     * Get the index of position of the import.
     *
     * @param imp the import.
     * @return the index of the define or -1.
     */
    @FromAnyThread
    public int indexOf(@NotNull final ShaderNodeDefinitionImport imp) {
        return ShaderNodeUtils.getImports(definition).indexOf(imp.getPath());
    }

    /**
     * Remove the old import from the shader node definition.
     *
     * @param imp the old import.
     */
    @FromAnyThread
    public void remove(@NotNull final ShaderNodeDefinitionImport imp) {
        ShaderNodeUtils.getImports(definition).remove(imp.getPath());
    }

    /**
     * Change the path of the import.
     *
     * @param imp     the import.
     * @param newPath the new name.
     */
    @FromAnyThread
    public void changePath(@NotNull final ShaderNodeDefinitionImport imp, @NotNull final String newPath) {
        final List<String> imports = ShaderNodeUtils.getImports(definition);
        final int position = imports.indexOf(imp.getPath());
        imports.remove(position);
        imports.add(position, newPath);
        imp.setPath(newPath);
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
        final ShaderNodeDefinitionImports that = (ShaderNodeDefinitionImports) o;
        return definition.equals(that.definition);
    }

    @Override
    public int hashCode() {
        return definition.hashCode();
    }
}
