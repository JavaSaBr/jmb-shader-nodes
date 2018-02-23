package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.remove;

import static com.ss.editor.shader.nodes.util.MaterialDefUtils.getMatParams;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * The implementation of removing old material parameter.
 *
 * @author JavaSaBr
 */
public class RemoveMaterialParameterVariableOperation extends RemoveUniformVariableOperation {

    /**
     * The material definition.
     */
    @NotNull
    private final MaterialDef materialDef;

    /**
     * The material parameter.
     */
    @NotNull
    private final MatParam matParam;

    public RemoveMaterialParameterVariableOperation(@NotNull final List<ShaderNode> shaderNodes,
                                                    @NotNull final MaterialDef materialDef,
                                                    @NotNull final TechniqueDef techniqueDef, @NotNull final MatParam matParam,
                                                    @NotNull final ShaderNodeVariable variable,
                                                    @NotNull final Vector2f location) {
        super(shaderNodes, techniqueDef, variable, location);
        this.materialDef = materialDef;
        this.matParam = matParam;
    }

    @Override
    @JmeThread
    protected void redoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJmeThread(editor);

        final Map<String, MatParam> matParams = getMatParams(materialDef);
        matParams.remove(matParam.getName());
    }

    @Override
    @FxThread
    protected void redoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFxThread(editor);
        editor.notifyRemovedMatParameter(matParam);
    }

    @Override
    @JmeThread
    protected void undoImplInJmeThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJmeThread(editor);

        final Map<String, MatParam> matParams = getMatParams(materialDef);
        matParams.put(matParam.getName(), matParam);
    }

    @Override
    @FxThread
    protected void undoImplInFxThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFxThread(editor);
        editor.notifyAddedMatParameter(matParam, location);
    }
}
