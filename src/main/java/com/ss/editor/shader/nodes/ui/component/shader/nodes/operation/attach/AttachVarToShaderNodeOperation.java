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

    public AttachVarToShaderNodeOperation(@NotNull final ShaderNode shaderNode,
                                          @Nullable final VariableMapping newMapping,
                                          @Nullable final VariableMapping oldMapping,
                                          @NotNull final TechniqueDef techniqueDef,
                                          @NotNull final ShaderNode outShaderNode) {
        super(shaderNode, newMapping, oldMapping);
        this.techniqueDef = techniqueDef;
        this.outShaderNode = outShaderNode;
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);

        final ShaderNode shaderNode = getShaderNode();
        final Shader.ShaderType inType = shaderNode.getDefinition().getType();

        final ShaderNode outShaderNode = getOutShaderNode();
        final Shader.ShaderType outType = outShaderNode.getDefinition().getType();

        final VariableMapping newMapping = getNewMapping();
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();

        if (newMapping != null && outType == Shader.ShaderType.Vertex && inType != Shader.ShaderType.Vertex) {

            final List<ShaderNodeVariable> varyings = generationInfo.getVaryings();
            final ShaderNodeVariable rightVar = newMapping.getRightVariable();

            if (!varyings.contains(rightVar)) {
                varyings.add(rightVar);
                toRevertOutput = findInMappingByNNLeftVar(outShaderNode, rightVar);
                wasAddedToVaryings = true;
            }
        }

        if (toRevertOutput != null) {
            toRevertOutput.getLeftVariable().setShaderOutput(true);
        }

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();
        final int outSnIndex = shaderNodes.indexOf(outShaderNode);
        final int snIndex = shaderNodes.indexOf(shaderNode);

        if (outSnIndex > snIndex) {
            previousShaderOrder = new ArrayList<>(shaderNodes);
            shaderNodes.remove(outShaderNode);
            shaderNodes.add(snIndex, outShaderNode);
        }

        final List<String> unusedNodes = generationInfo.getUnusedNodes();
        if (unusedNodes.contains(outShaderNode.getName())) {
            unusedNodes.remove(outShaderNode.getName());
            outShaderWasUnused = true;
        }

        final List<VariableMapping> inputMapping = shaderNode.getInputMapping();

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

        final VariableMapping newMapping = getNewMapping();
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();

        if (newMapping != null && wasAddedToVaryings) {
            final List<ShaderNodeVariable> varyings = generationInfo.getVaryings();
            varyings.remove(newMapping.getRightVariable());
            wasAddedToVaryings = false;
        }

        if (toRevertOutput != null) {
            toRevertOutput.getLeftVariable().setShaderOutput(false);
            toRevertOutput = null;
        }

        if (previousShaderOrder != null) {
            final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();
            shaderNodes.clear();
            shaderNodes.addAll(previousShaderOrder);
            previousShaderOrder = null;
        }

        if (outShaderWasUnused) {
            final List<String> unusedNodes = generationInfo.getUnusedNodes();
            unusedNodes.add(outShaderNode.getName());
            outShaderWasUnused = false;
        }

        final List<VariableMapping> inputMapping = getShaderNode().getInputMapping();

        if (newMapping != null) {
            inputMapping.remove(newMapping);
        }

        if (getOldMapping() != null) {
            inputMapping.add(getOldMapping());
        }
    }

    /**
     * @return the output shader nodes.
     */
    @FromAnyThread
    private @NotNull ShaderNode getOutShaderNode() {
        return outShaderNode;
    }
}
