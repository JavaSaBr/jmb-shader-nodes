package com.ss.editor.shader.nodes.ui.component.preview.material.definition;

import static com.ss.rlib.util.ObjectUtils.notNull;
import static com.ss.rlib.util.Utils.get;
import com.jme3.asset.AssetManager;
import com.jme3.material.MaterialDef;
import com.jme3.material.plugin.export.materialdef.J3mdExporter;
import com.jme3.renderer.RenderManager;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.component.preview.CodePreviewComponent;
import com.ss.editor.ui.control.code.BaseCodeArea;
import com.ss.editor.ui.control.code.MaterialDefinitionCodeArea;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;

/**
 * The base implementation of preview a result material definition.
 *
 * @author JavaSaBr
 */
public class MaterialDefCodePreviewComponent extends CodePreviewComponent {

    public MaterialDefCodePreviewComponent(@NotNull final AssetManager assetManager,
                                           @NotNull final RenderManager renderManager) {
        super(assetManager, renderManager);
    }

    @Override
    @FxThread
    protected void createComponents() {

        codeArea = new MaterialDefinitionCodeArea();
        codeArea.prefHeightProperty().bind(heightProperty());
        codeArea.loadContent("");
        codeArea.setEditable(false);

        FXUtils.addToPane(codeArea, this);
    }

    /**
     * Load the material definition.
     *
     * @param materialDef the material definition.
     */
    @FxThread
    public void load(@NotNull final MaterialDef materialDef) {

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final J3mdExporter exporter = new J3mdExporter();

        Utils.run(() -> exporter.save(materialDef, bout));

        final BaseCodeArea codeArea = getCodeArea();
        codeArea.reloadContent(notNull(get(() -> new String(bout.toByteArray(), "UTF-8"))));
    }
}

