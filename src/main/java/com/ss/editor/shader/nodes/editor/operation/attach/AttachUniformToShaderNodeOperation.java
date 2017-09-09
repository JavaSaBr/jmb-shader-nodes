package com.ss.editor.shader.nodes.editor.operation.attach;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of attaching an uniform parameter to a shader node.
 *
 * @author JavaSaBr
 */
public class AttachUniformToShaderNodeOperation extends AttachShaderNodeOperation {

    /**
     * The source variable.
     */
    @NotNull
    private final ShaderNodeVariable source;

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * True if source variable was added to uniforms.
     */
    private boolean wasAddedToUnforms;

    public AttachUniformToShaderNodeOperation(@NotNull final ShaderNode shaderNode,
                                              @NotNull final ShaderNodeVariable source,
                                              @NotNull final TechniqueDef techniqueDef,
                                              @Nullable final VariableMapping newMapping,
                                              @Nullable final VariableMapping oldMapping) {
        super(shaderNode, newMapping, oldMapping);
        this.source = source;
        this.techniqueDef = techniqueDef;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        final ShaderNode shaderNode = getShaderNode();
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> uniforms;

        if (shaderNode.getDefinition().getType() == Shader.ShaderType.Vertex) {
            uniforms = generationInfo.getVertexUniforms();
        } else {
            uniforms = generationInfo.getFragmentUniforms();
        }

        if (!uniforms.contains(source)) {
            uniforms.add(source);
            wasAddedToUnforms = true;
        }

        final List<VariableMapping> inputMapping = shaderNode.getInputMapping();

        if (getOldMapping() != null) {
            inputMapping.remove(getOldMapping());
        }

        if (getNewMapping() != null) {
            inputMapping.add(getNewMapping());
        }
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final ShaderNode shaderNode = getShaderNode();
        final ShaderGenerationInfo generationInfo = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> uniforms;

        if (shaderNode.getDefinition().getType() == Shader.ShaderType.Vertex) {
            uniforms = generationInfo.getVertexUniforms();
        } else {
            uniforms = generationInfo.getFragmentUniforms();
        }

        if (wasAddedToUnforms) {
            uniforms.remove(source);
            wasAddedToUnforms = false;
        }

        final List<VariableMapping> inputMapping = getShaderNode().getInputMapping();

        if (getNewMapping() != null) {
            inputMapping.remove(getNewMapping());
        }

        if (getOldMapping() != null) {
            inputMapping.add(getOldMapping());
        }
    }
}
