package com.ss.editor.shader.nodes.component.shader.node.operation.attach;

import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingByNNLeftVar;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of attaching a variable to a shader node.
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
     * The output shader node.
     */
    @NotNull
    private ShaderNode outShaderNode;

    /**
     * The mapping to revert output status of the left variable.
     */
    @Nullable
    private VariableMapping toRevertOutput;

    /**
     * True if source variable was added to varyings.
     */
    private boolean wasAddedToVaryings;

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
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

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

        final List<VariableMapping> inputMapping = shaderNode.getInputMapping();

        if (getOldMapping() != null) {
            inputMapping.remove(getOldMapping());
        }

        if (newMapping != null) {
            inputMapping.add(newMapping);
        }
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final VariableMapping newMapping = getNewMapping();

        if (newMapping != null && wasAddedToVaryings) {
            final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();
            final List<ShaderNodeVariable> varyings = generationInfo.getVaryings();
            varyings.remove(newMapping.getRightVariable());
            wasAddedToVaryings = false;
        }

        if (toRevertOutput != null) {
            toRevertOutput.getLeftVariable().setShaderOutput(false);
            toRevertOutput = null;
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
     * @return the output shader node.
     */
    @FromAnyThread
    private @NotNull ShaderNode getOutShaderNode() {
        return outShaderNode;
    }
}
