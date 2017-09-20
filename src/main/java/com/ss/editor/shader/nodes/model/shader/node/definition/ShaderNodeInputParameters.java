package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The class to present input parameters of a shader node.
 *
 * @author JavaSaBr
 */
public class ShaderNodeInputParameters extends ShaderNodeParameters {

    public ShaderNodeInputParameters(@NotNull final ShaderNodeDefinition definition) {
        super(definition);
    }

    @Override
    @FromAnyThread
    public @NotNull List<ShaderNodeVariable> getParameters() {
        return getDefinition().getInputs();
    }
}
