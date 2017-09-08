package com.ss.editor.shader.nodes.editor.operation;

import static com.ss.editor.shader.nodes.util.MaterialDefUtils.getMatParams;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.math.Vector2f;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The implementation of adding new material parameter.
 *
 * @author JavaSaBr
 */
public class AddMatParamShaderNodeOperation extends ShaderNodeOperation {

    @NotNull
    private final MaterialDef materialDef;

    @NotNull
    private final MatParam matParam;

    @NotNull
    private final Vector2f location;

    public AddMatParamShaderNodeOperation(@NotNull final MaterialDef materialDef, @NotNull final MatParam matParam,
                                          @NotNull final Vector2f location) {
        this.materialDef = materialDef;
        this.matParam = matParam;
        this.location = location;
    }

    @Override
    @JMEThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        final Map<String, MatParam> matParams = getMatParams(materialDef);
        matParams.put(matParam.getName(), matParam);
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyAddedMatParameter(matParam, location);
    }


    @Override
    @JMEThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);

        final Map<String, MatParam> matParams = getMatParams(materialDef);
        matParams.remove(matParam.getName());
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyRemovedMatParameter(matParam);
    }
}