package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.attach;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingByNNLeftVar;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of attaching a variable to a shader nodes.
 *
 * @author JavaSaBr
 */
public class AttachVarToShaderNodeOperation extends AttachShaderNodeOperation {

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * The output shader nodes.
     */
    @NotNull
    private ShaderNode outShaderNode;

    /**
     * The mapping to revert output status of the left variable.
     */
    @Nullable
    private VariableMapping toRevertOutput;

    /**
     * The previous order of shader nodes.
     */
    @Nullable
    private List<ShaderNode> previousShaderOrder;

    /**
     * True if source variable was added to varyings.
     */
    private boolean wasAddedToVaryings;

    /**
     * True of the output shader was unused.
     */
    private boolean outShaderWasUnused;

    public AttachVarToShaderNodeOperation(
        @NotNull final ShaderNode shaderNode,
        @Nullable final VariableMapping newMapping,
        @Nullable final VariableMapping oldMapping,
        @NotNull final TechniqueDef techniqueDef,
        @NotNull final ShaderNode outShaderNode
    ) {
        super(shaderNode, newMapping, oldMapping);
        this.techniqueDef = techniqueDef;
        this.outShaderNode = outShaderNode;
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);

        var shaderNode = getShaderNode();
        var inType = shaderNode.getDefinition().getType();

        var outShaderNode = getOutShaderNode();
        var outType = outShaderNode.getDefinition().getType();

        var newMapping = getNewMapping();
        var generationInfo = techniqueDef.getShaderGenerationInfo();

        if (newMapping != null && outType == Shader.ShaderType.Vertex && inType != Shader.ShaderType.Vertex) {

            var varyings = generationInfo.getVaryings();
            var rightVar = newMapping.getRightVariable();

            if (!varyings.contains(rightVar)) {
                varyings.add(rightVar);
                toRevertOutput = findInMappingByNNLeftVar(outShaderNode, rightVar);
                wasAddedToVaryings = true;
            }
        }

        if (toRevertOutput != null) {
            toRevertOutput.getLeftVariable().setShaderOutput(true);
        }

        var shaderNodes = techniqueDef.getShaderNodes();
        var outSnIndex = shaderNodes.indexOf(outShaderNode);
        var snIndex = shaderNodes.indexOf(shaderNode);

        if (outSnIndex > snIndex) {
            previousShaderOrder = new ArrayList<>(shaderNodes);
            shaderNodes.remove(outShaderNode);
            shaderNodes.add(snIndex, outShaderNode);
        }

        var unusedNodes = generationInfo.getUnusedNodes();
        if (unusedNodes.contains(outShaderNode.getName())) {
            unusedNodes.remove(outShaderNode.getName());
            outShaderWasUnused = true;
        }

        var inputMapping = shaderNode.getInputMapping();

        if (getOldMapping() != null) {
            inputMapping.remove(getOldMapping());
        }

        if (newMapping != null) {
            inputMapping.add(newMapping);
        }
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);

        var newMapping = getNewMapping();
        var generationInfo = techniqueDef.getShaderGenerationInfo();

        if (newMapping != null && wasAddedToVaryings) {
            var varyings = generationInfo.getVaryings();
            varyings.remove(newMapping.getRightVariable());
            wasAddedToVaryings = false;
        }

        if (toRevertOutput != null) {
            toRevertOutput.getLeftVariable().setShaderOutput(false);
            toRevertOutput = null;
        }

        if (previousShaderOrder != null) {
            var shaderNodes = techniqueDef.getShaderNodes();
            shaderNodes.clear();
            shaderNodes.addAll(previousShaderOrder);
            previousShaderOrder = null;
        }

        if (outShaderWasUnused) {
            var unusedNodes = generationInfo.getUnusedNodes();
            unusedNodes.add(outShaderNode.getName());
            outShaderWasUnused = false;
        }

        var inputMapping = getShaderNode().getInputMapping();
        if (newMapping != null) {
            inputMapping.remove(newMapping);
        }

        if (getOldMapping() != null) {
            inputMapping.add(getOldMapping());
        }
    }

    /**
     * Get the output shader node.
     *
     * @return the output shader node.
     */
    @FromAnyThread
    private @NotNull ShaderNode getOutShaderNode() {
        return outShaderNode;
    }
}
