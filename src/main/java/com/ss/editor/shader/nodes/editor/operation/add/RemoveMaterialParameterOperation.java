package com.ss.editor.shader.nodes.editor.operation.add;

import static com.ss.editor.shader.nodes.util.MaterialDefUtils.getMatParams;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.findInMappingByNNRightVar;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.operation.ShaderNodeOperation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The implementation of removing old material parameter.
 *
 * @author JavaSaBr
 */
public class RemoveMaterialParameterOperation extends ShaderNodeOperation {

    /**
     * The map of shader nodes to restore references to the parameter.
     */
    @NotNull
    private final Map<ShaderNode, VariableMapping> toRestore;

    /**
     * The list of using shader nodes.
     */
    @NotNull
    private final List<ShaderNode> shaderNodes;

    /**
     * The material definition.
     */
    @NotNull
    private final MaterialDef materialDef;

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * The material parameter.
     */
    @NotNull
    private final MatParam matParam;

    /**
     * The current variable.
     */
    @NotNull
    private final ShaderNodeVariable variable;

    /**
     * The last location.
     */
    @NotNull
    private final Vector2f location;

    /**
     * The variable from vertex uniforms.
     */
    @Nullable
    private ShaderNodeVariable vertextUniform;

    /**
     * The variable from fragment uniforms.
     */
    @Nullable
    private ShaderNodeVariable fragmentUniform;

    public RemoveMaterialParameterOperation(@NotNull final List<ShaderNode> shaderNodes,
                                            @NotNull final MaterialDef materialDef,
                                            @NotNull final TechniqueDef techniqueDef, @NotNull final MatParam matParam,
                                            @NotNull final ShaderNodeVariable variable,
                                            @NotNull final Vector2f location) {
        this.toRestore = new HashMap<>();
        this.shaderNodes = shaderNodes;
        this.materialDef = materialDef;
        this.techniqueDef = techniqueDef;
        this.matParam = matParam;
        this.variable = variable;
        this.location = location;
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

        for (final ShaderNode shaderNode : shaderNodes) {
            final VariableMapping mapping = findInMappingByNNRightVar(shaderNode, variable);
            if (mapping != null) {
                shaderNode.getInputMapping().remove(mapping);
                toRestore.put(shaderNode, mapping);
            }
        }

        final Map<String, MatParam> matParams = getMatParams(materialDef);
        matParams.remove(matParam.getName());
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyRemovedMatParameter(matParam);
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

        for (final Map.Entry<ShaderNode, VariableMapping> entry : toRestore.entrySet()) {
            final ShaderNode shaderNode = entry.getKey();
            shaderNode.getInputMapping().add(entry.getValue());
        }

        toRestore.clear();

        final Map<String, MatParam> matParams = getMatParams(materialDef);
        matParams.put(matParam.getName(), matParam);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyAddedMatParameter(matParam, location);
    }
}
