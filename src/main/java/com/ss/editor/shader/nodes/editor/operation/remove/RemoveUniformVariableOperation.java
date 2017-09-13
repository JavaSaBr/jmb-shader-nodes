package com.ss.editor.shader.nodes.editor.operation.remove;

import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The implementation of removing an uniform.
 *
 * @author JavaSaBr
 */
public class RemoveUniformVariableOperation extends RemoveVariableOperation {

    /**
     * The variable from vertex uniforms.
     */
    @Nullable
    protected ShaderNodeVariable vertextUniform;

    /**
     * The variable from fragment uniforms.
     */
    @Nullable
    protected ShaderNodeVariable fragmentUniform;

    public RemoveUniformVariableOperation(@NotNull final List<ShaderNode> shaderNodes, @NotNull final TechniqueDef techniqueDef,
                                          @NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {

        super(shaderNodes, techniqueDef, variable, location);
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> vertexUniforms = info.getVertexUniforms();
        final List<ShaderNodeVariable> fragmentUniforms = info.getFragmentUniforms();

        if (vertexUniforms.contains(variable)) {
            vertextUniform = vertexUniforms.remove(vertexUniforms.indexOf(variable));
        }

        if (fragmentUniforms.contains(variable)) {
            fragmentUniform = fragmentUniforms.remove(fragmentUniforms.indexOf(variable));
        }
    }

    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> vertexUniforms = info.getVertexUniforms();
        final List<ShaderNodeVariable> fragmentUniforms = info.getFragmentUniforms();

        if (vertextUniform != null) {
            vertexUniforms.add(vertextUniform);
            vertextUniform = null;
        }

        if (fragmentUniform != null) {
            fragmentUniforms.add(fragmentUniform);
            fragmentUniform = null;
        }
    }
}
