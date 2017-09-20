package com.ss.editor.shader.nodes.component.preview;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetManager;
import com.jme3.renderer.RenderManager;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.ui.control.code.BaseCodeArea;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base implementation of preview shader component.
 *
 * @author JavaSaBr
 */
public abstract class CodePreviewComponent extends VBox {

    /**
     * The asset manager.
     */
    @NotNull
    private final AssetManager assetManager;

    /**
     * The render manager.
     */
    @NotNull
    private final RenderManager renderManager;

    /**
     * The code area.
     */
    @Nullable
    protected BaseCodeArea codeArea;

    public CodePreviewComponent(@NotNull final AssetManager assetManager, @NotNull final RenderManager renderManager) {
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        createComponents();
    }

    @FXThread
    protected abstract void createComponents();

    /**
     * Get the render manager.
     *
     * @return the render manager.
     */
    @FXThread
    protected @NotNull RenderManager getRenderManager() {
        return renderManager;
    }

    /**
     * Get the asset manager.
     *
     * @return the asset manager.
     */
    @FXThread
    protected @NotNull AssetManager getAssetManager() {
        return assetManager;
    }

    /**
     * Get the code area.
     *
     * @return the code area.
     */
    @FXThread
    protected @NotNull BaseCodeArea getCodeArea() {
        return notNull(codeArea);
    }
}
