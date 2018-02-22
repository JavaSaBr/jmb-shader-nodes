package com.ss.editor.shader.nodes.ui.component.preview.shader;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.RenderManager;
import com.jme3.shader.Shader;
import com.ss.editor.annotation.FxThread;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of preview a result fragment shader.
 *
 * @author JavaSaBr
 */
public class FragmentShaderCodePreviewComponent extends ShaderCodePreviewComponent {

    public FragmentShaderCodePreviewComponent(@NotNull final AssetManager assetManager,
                                              @NotNull final RenderManager renderManager) {
        super(assetManager, renderManager);
    }

    @Override
    @FxThread
    protected @NotNull Shader.ShaderType getShaderType() {
        return Shader.ShaderType.Fragment;
    }
}
