package com.ss.editor.shader.nodes.component.shader.node.operation.remove;

import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.ss.editor.shader.nodes.component.shader.node.operation.ShaderNodeOperation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation of removing operation.
 *
 * @author JavaSaBr
 */
public class RemoveOperation extends ShaderNodeOperation {

    /**
     * The map of shader nodes to restore references to the parameter.
     */
    @NotNull
    protected final Map<ShaderNode, List<VariableMapping>> toRestore;

    /**
     * The list of using shader nodes.
     */
    @NotNull
    protected final List<ShaderNode> shaderNodes;

    /**
     * The technique definition.
     */
    @NotNull
    protected final TechniqueDef techniqueDef;

    /**
     * The last location.
     */
    @NotNull
    protected final Vector2f location;

    public RemoveOperation(@NotNull final List<ShaderNode> shaderNodes, @NotNull final TechniqueDef techniqueDef,
                           @NotNull final Vector2f location) {
        this.toRestore = new HashMap<>();
        this.shaderNodes = shaderNodes;
        this.techniqueDef = techniqueDef;
        this.location = location;
    }
}
