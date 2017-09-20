package com.ss.editor.shader.nodes.component.preview.shader;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.RenderManager;
import com.jme3.shader.Shader;
import com.ss.editor.annotation.FXThread;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of preview a result vertex shader.
 *
 * @author JavaSaBr
 */
public class VertexShaderCodePreviewComponent extends ShaderCodePreviewComponent {

    public VertexShaderCodePreviewComponent(@NotNull final AssetManager assetManager,
                                            @NotNull final RenderManager renderManager) {
        super(assetManager, renderManager);
    }

    @Override
    @FXThread
    protected @NotNull Shader.ShaderType getShaderType() {
        return Shader.ShaderType.Vertex;
    }
}
