package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

/**
 * The class to present a define of a shader node definition.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionDefine {

    /**
     * The definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    /**
     * The name of the define.
     */
    @NotNull
    private String define;

    public ShaderNodeDefinitionDefine(@NotNull final ShaderNodeDefinition definition, @NotNull final String define) {
        this.definition = definition;
        this.define = define;
    }

    /**
     * Get the name of the define.
     *
     * @return the name of the define.
     */
    @FromAnyThread
    public @NotNull String getDefine() {
        return define;
    }

    /**
     * Set the name of the define.
     *
     * @param define the name of the define.
     */
    @FromAnyThread
    public void setDefine(@NotNull final String define) {
        this.define = define;
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
        final ShaderNodeDefinitionDefine that = (ShaderNodeDefinitionDefine) obj;
        if (!definition.equals(that.definition)) return false;
        return define.equals(that.define);
    }

    @Override
    public int hashCode() {
        int result = definition.hashCode();
        result = 31 * result + define.hashCode();
        return result;
    }
}
