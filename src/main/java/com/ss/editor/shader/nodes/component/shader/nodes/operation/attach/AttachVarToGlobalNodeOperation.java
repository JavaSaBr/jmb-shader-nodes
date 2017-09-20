package com.ss.editor.shader.nodes.component.shader.nodes.operation.attach;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findOutMappingByNNLeftVar;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Current attached nodes to the global nodes.
     */
    @NotNull
    private final List<ShaderNode> currentNodes;

    /**
     * The removed output mappings from other shader nodes.
     */
    @NotNull
    private final Map<ShaderNode, VariableMapping> mappingsToRestore;

    public AttachVarToGlobalNodeOperation(@NotNull final ShaderNode shaderNode,
                                          @Nullable final VariableMapping newMapping,
                                          @Nullable final VariableMapping oldMapping,
                                          @NotNull final List<ShaderNode> currentNodes) {
        super(shaderNode, newMapping, oldMapping);
        this.currentNodes = currentNodes;
        this.mappingsToRestore = new HashMap<>(currentNodes.size());
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        final List<VariableMapping> outputMapping = getShaderNode().getOutputMapping();

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
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final List<VariableMapping> outputMapping = getShaderNode().getOutputMapping();

        if (getNewMapping() != null) {
            mappingsToRestore.forEach((shaderNode, mapping) -> shaderNode.getOutputMapping().add(mapping));
            mappingsToRestore.clear();
            outputMapping.remove(getNewMapping());
        }

        if (getOldMapping() != null) {
            outputMapping.add(getOldMapping());
        }
    }
}
