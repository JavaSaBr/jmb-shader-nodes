package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.remove;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingsByNNRightVar;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
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
     * The current shader nodes.
     */
    @NotNull
    protected final ShaderNode shaderNode;

    /**
     * The order of the nodes.
     */
    private int order;

    public RemoveShaderNodeOperation(@NotNull final List<ShaderNode> shaderNodes, @NotNull final TechniqueDef techniqueDef,
                                     @NotNull final ShaderNode shaderNode, @NotNull final Vector2f location) {
        super(shaderNodes, techniqueDef, location);
        this.shaderNode = shaderNode;
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);

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
    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFxThread(editor);
        editor.notifyRemovedRemovedShaderNode(shaderNode);
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);

        for (final Map.Entry<ShaderNode, List<VariableMapping>> entry : toRestore.entrySet()) {
            final ShaderNode shaderNode = entry.getKey();
            shaderNode.getInputMapping().addAll(entry.getValue());
        }

        toRestore.clear();

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();
        shaderNodes.add(order, shaderNode);
    }

    @Override
    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFxThread(editor);
        editor.notifyAddedShaderNode(shaderNode, location);
    }
}
