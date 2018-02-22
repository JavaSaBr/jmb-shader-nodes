package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findOutMappingByNNLeftVar;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation of attaching a variable to a global nodes.
 *
 * @author JavaSaBr
 */
public class AttachVarToGlobalNodeOperation extends AttachShaderNodeOperation {

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * Current attached nodes to the global nodes.
     */
    @NotNull
    private final List<ShaderNode> currentNodes;

    /**
     * The list of used nodes.
     */
    @NotNull
    private final List<ShaderNode> usedNodes;

    /**
     * The list of shader nodes to disable after reverting.
     */
    @NotNull
    private final List<ShaderNode> toDisable;

    /**
     * The removed output mappings from other shader nodes.
     */
    @NotNull
    private final Map<ShaderNode, VariableMapping> mappingsToRestore;

    public AttachVarToGlobalNodeOperation(@NotNull final TechniqueDef techniqueDef,
                                          @NotNull final ShaderNode shaderNode,
                                          @Nullable final VariableMapping newMapping,
                                          @Nullable final VariableMapping oldMapping,
                                          @NotNull final List<ShaderNode> currentNodes,
                                          @NotNull final List<ShaderNode> usedNodes) {
        super(shaderNode, newMapping, oldMapping);
        this.techniqueDef = techniqueDef;
        this.currentNodes = currentNodes;
        this.mappingsToRestore = new HashMap<>(currentNodes.size());
        this.usedNodes = usedNodes;
        this.toDisable = new ArrayList<>(usedNodes.size());
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);

        final List<VariableMapping> outputMapping = getShaderNode().getOutputMapping();
        final TechniqueDef techniqueDef = getTechniqueDef();
        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();

        if (getOldMapping() != null) {
            outputMapping.remove(getOldMapping());
        }

        final VariableMapping newMapping = getNewMapping();

        if (newMapping != null) {

            final ShaderNodeVariable leftVariable = newMapping.getLeftVariable();

            for (final ShaderNode currentNode : currentNodes) {

                final List<VariableMapping> otherMappings = currentNode.getOutputMapping();
                final VariableMapping variableMapping = findOutMappingByNNLeftVar(currentNode, leftVariable);

                if (variableMapping != null) {
                    mappingsToRestore.put(currentNode, variableMapping);
                    otherMappings.remove(variableMapping);
                }
            }

            outputMapping.add(getNewMapping());
        }

        final List<ShaderNode> toDisable = getToDisable();
        final List<String> unusedNodes = info.getUnusedNodes();

        for (final ShaderNode usedNode : usedNodes) {
            if (unusedNodes.contains(usedNode.getName())) {
                toDisable.add(usedNode);
                unusedNodes.remove(usedNode.getName());
            }
        }
    }

    /**
     * Get the list of shader nodes to disable after reverting.
     *
     * @return the list of shader nodes to disable after reverting.
     */
    @FromAnyThread
    private @NotNull List<ShaderNode> getToDisable() {
        return toDisable;
    }

    /**
     * Get the list of used nodes.
     *
     * @return the list of used nodes.
     */
    @FromAnyThread
    private @NotNull List<ShaderNode> getUsedNodes() {
        return usedNodes;
    }

    /**
     * Get the technique definition.
     *
     * @return the technique definition.
     */
    @FromAnyThread
    private @NotNull TechniqueDef getTechniqueDef() {
        return techniqueDef;
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);

        final TechniqueDef techniqueDef = getTechniqueDef();
        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();

        final List<VariableMapping> outputMapping = getShaderNode().getOutputMapping();

        if (getNewMapping() != null) {
            mappingsToRestore.forEach((shaderNode, mapping) -> shaderNode.getOutputMapping().add(mapping));
            mappingsToRestore.clear();
            outputMapping.remove(getNewMapping());
        }

        if (getOldMapping() != null) {
            outputMapping.add(getOldMapping());
        }

        final List<ShaderNode> toDisable = getToDisable();
        final List<String> unusedNodes = info.getUnusedNodes();

        for (final ShaderNode usedNode : toDisable) {
            unusedNodes.add(usedNode.getName());
        }

        toDisable.clear();
    }
}
