package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation;

import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.material.logic.*;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The operation ot change light mode of {@link com.jme3.material.TechniqueDef}.
 *
 * @author JavaSaBr
 */
public class ChangeLightModeOperation extends ShaderNodeOperation {

    @NotNull
    private final String techniqueName;

    @NotNull
    private final TechniqueDef.LightMode prevLightMode;

    @NotNull
    private final TechniqueDef.LightMode newLightMode;

    @Nullable
    private TechniqueDef techniqueDef;

    public ChangeLightModeOperation(@NotNull final String techniqueName,
                                    @NotNull final TechniqueDef.LightMode prevLightMode,
                                    @NotNull final TechniqueDef.LightMode newLightMode) {
        this.techniqueName = techniqueName;
        this.prevLightMode = prevLightMode;
        this.newLightMode = newLightMode;
    }

    @Override
    @FXThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);

        final MaterialDef materialDef = editor.getMaterialDef();
        final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(techniqueName);

        if (techniqueDefs.isEmpty()) {
            throw new RuntimeException("Couldn't find a technique definition for the name " + techniqueName);
        }

        techniqueDef = techniqueDefs.get(0);
        techniqueDef.setLightMode(newLightMode);

        createLogic(techniqueDef);

        editor.notifyFXChangeProperty(techniqueDef, Messages.MODEL_PROPERTY_LIGHT_MODE);
    }

    @Override
    @FXThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);

        final MaterialDef materialDef = editor.getMaterialDef();
        final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(techniqueName);

        if (techniqueDefs.isEmpty()) {
            throw new RuntimeException("Couldn't find a technique definition for the name " + techniqueName);
        }

        techniqueDef = techniqueDefs.get(0);
        techniqueDef.setLightMode(prevLightMode);

        createLogic(techniqueDef);

        editor.notifyFXChangeProperty(techniqueDef, Messages.MODEL_PROPERTY_LIGHT_MODE);
    }

    /**
     * Create the logic for the technique def.
     *
     * @param techniqueDef the technique def.
     */
    @FXThread
    private void createLogic(@NotNull final TechniqueDef techniqueDef) {
        switch (techniqueDef.getLightMode()) {
            case SinglePass: {
                techniqueDef.setLogic(new SinglePassLightingLogic(techniqueDef));
                break;
            }
            case SinglePassAndImageBased: {
                techniqueDef.setLogic(new SinglePassAndImageBasedLightingLogic(techniqueDef));
                break;
            }
            case StaticPass: {
                techniqueDef.setLogic(new StaticPassLightingLogic(techniqueDef));
                break;
            }
            case MultiPass: {
                techniqueDef.setLogic(new MultiPassLightingLogic(techniqueDef));
                break;
            }
            case Disable: {
                techniqueDef.setLogic(new DefaultTechniqueDefLogic(techniqueDef));
            }
        }
    }
}
