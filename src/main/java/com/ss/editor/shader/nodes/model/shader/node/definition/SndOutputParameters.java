package com.ss.editor.shader.nodes.model.shader.node.definition;

import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FromAnyThread;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The class to present output parameters of a shader node definition.
 *
 * @author JavaSaBr
 */
public class SndOutputParameters extends SndParameters {

    public SndOutputParameters(@NotNull final ShaderNodeDefinition definition) {
        super(definition);
    }

    @Override
    @FromAnyThread
    public @NotNull List<ShaderNodeVariable> getParameters() {
        return getDefinition().getOutputs();
    }
}
