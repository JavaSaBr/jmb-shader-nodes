package com.ss.editor.shader.nodes.editor.operation.remove;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingsByNNRightVar;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The implementation of removing operation.
 *
 * @author JavaSaBr
 */
public class RemoveShaderNodeOperation extends RemoveOperation {

    /**
     * The current shader node.
     */
    @NotNull
    protected final ShaderNode shaderNode;

    /**
     * The order of the node.
     */
    private int order;

    public RemoveShaderNodeOperation(@NotNull final List<ShaderNode> shaderNodes, @NotNull final TechniqueDef techniqueDef,
                                     @NotNull final ShaderNode shaderNode, @NotNull final Vector2f location) {
        super(shaderNodes, techniqueDef, location);
        this.shaderNode = shaderNode;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        for (final ShaderNode otherNode : shaderNodes) {

            final ShaderNodeDefinition definition = otherNode.getDefinition();
            final List<ShaderNodeVariable> outputs = definition.getOutputs();
            final List<VariableMapping> resultMapping = new ArrayList<>();

            for (final ShaderNodeVariable outVar : outputs) {
                resultMapping.addAll(findInMappingsByNNRightVar(otherNode, outVar, shaderNode.getName()));
            }

            if (!resultMapping.isEmpty()) {
                otherNode.getInputMapping().removeAll(resultMapping);
                toRestore.put(otherNode, resultMapping);
            }
        }

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();
        order = shaderNodes.indexOf(shaderNode);
        shaderNodes.remove(shaderNode);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyRemovedRemovedShaderNode(shaderNode);
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

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();
        shaderNodes.add(order, shaderNode);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyAddedShaderNode(shaderNode, location);
    }
}
