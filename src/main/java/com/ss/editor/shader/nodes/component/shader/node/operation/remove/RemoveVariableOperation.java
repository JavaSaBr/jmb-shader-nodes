package com.ss.editor.shader.nodes.component.shader.node.operation.remove;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingsByNNRightVar;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * The implementation of removing operation.
 *
 * @author JavaSaBr
 */
public class RemoveVariableOperation extends RemoveOperation {

    /**
     * The current variable.
     */
    @NotNull
    protected final ShaderNodeVariable variable;

    public RemoveVariableOperation(@NotNull final List<ShaderNode> shaderNodes, @NotNull final TechniqueDef techniqueDef,
                                   @NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {
        super(shaderNodes, techniqueDef, location);
        this.variable = variable;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        for (final ShaderNode shaderNode : shaderNodes) {
            final List<VariableMapping> mappings = findInMappingsByNNRightVar(shaderNode, variable);
            if (!mappings.isEmpty()) {
                shaderNode.getInputMapping().removeAll(mappings);
                toRestore.put(shaderNode, mappings);
            }
        }
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        for (final Map.Entry<ShaderNode, List<VariableMapping>> entry : toRestore.entrySet()) {
            final ShaderNode shaderNode = entry.getKey();
            shaderNode.getInputMapping().addAll(entry.getValue());
        }

        toRestore.clear();
    }
}
