package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FXThread;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The object with all definitions from shader node file.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionList {

    /**
     * The list of definitions.
     */
    @NotNull
    private List<ShaderNodeDefinition> definitions;

    public ShaderNodeDefinitionList(@NotNull final List<ShaderNodeDefinition> definitions) {
        this.definitions = definitions;
    }

    /**
     * Get the list of definitions.
     *
     * @return the list of definitions.
     */
    @FXThread
    public @NotNull List<ShaderNodeDefinition> getDefinitions() {
        return definitions;
    }
}
