package com.ss.editor.shader.nodes.editor.operation.remove;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingByNNRightVar;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.operation.ShaderNodeOperation;
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
    protected final Map<ShaderNode, VariableMapping> toRestore;

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
     * The current variable.
     */
    @NotNull
    protected final ShaderNodeVariable variable;

    /**
     * The last location.
     */
    @NotNull
    protected final Vector2f location;

    public RemoveOperation(@NotNull final List<ShaderNode> shaderNodes, @NotNull final TechniqueDef techniqueDef,
                           @NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {
        this.toRestore = new HashMap<>();
        this.shaderNodes = shaderNodes;
        this.techniqueDef = techniqueDef;
        this.variable = variable;
        this.location = location;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        for (final ShaderNode shaderNode : shaderNodes) {
            final VariableMapping mapping = findInMappingByNNRightVar(shaderNode, variable);
            if (mapping != null) {
                shaderNode.getInputMapping().remove(mapping);
                toRestore.put(shaderNode, mapping);
            }
        }
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();

        for (final Map.Entry<ShaderNode, VariableMapping> entry : toRestore.entrySet()) {
            final ShaderNode shaderNode = entry.getKey();
            shaderNode.getInputMapping().add(entry.getValue());
        }

        toRestore.clear();
    }
}
