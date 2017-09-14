package com.ss.editor.shader.nodes.creator;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.StreamAssetInfo;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.MaterialDef;
import com.jme3.material.plugin.export.materialdef.J3mdExporter;
import com.jme3.material.plugins.J3MLoader;
import com.ss.editor.annotation.BackgroundThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.plugin.api.file.creator.GenericFileCreator;
import com.ss.editor.shader.nodes.ShaderNodesEditorPlugin;
import com.ss.editor.shader.nodes.model.ShaderNodesProject;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.rlib.util.VarTable;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The implementation of creator to create a shader nodes project.
 *
 * @author JavaSaBr
 */
public class ShaderNodesProjectFileCreator extends GenericFileCreator {

    @NotNull
    private static final String MD_TEMPLATE = "/com/ss/editor/shader/nodes/template/MaterialDefinition.j3md";

    /**
     * The description of this creator.
     */
    @NotNull
    public static final FileCreatorDescription DESCRIPTION = new FileCreatorDescription();

    static {
        DESCRIPTION.setFileDescription("Create shader nodes project file");
        DESCRIPTION.setConstructor(ShaderNodesProjectFileCreator::new);
    }

    @Override
    @FromAnyThread
    protected @NotNull String getTitleText() {
        return "Creating new shader node material project";
    }

    @Override
    @FromAnyThread
    protected @NotNull String getFileExtension() {
        return ShaderNodesEditorPlugin.PROJECT_FILE_EXTENSION;
    }

    @Override
    @BackgroundThread
    protected void writeData(@NotNull final VarTable vars, @NotNull final Path resultFile) throws IOException {
        super.writeData(vars, resultFile);

        final InputStream in = getClass().getResourceAsStream(MD_TEMPLATE);
        final AssetManager assetManager = EDITOR.getAssetManager();
        final AssetKey<MaterialDef> assetKey = new AssetKey<>("tempMatDef");
        final StreamAssetInfo assetInfo = new StreamAssetInfo(assetManager, assetKey, in);

        final J3MLoader loader = new J3MLoader();
        final MaterialDef materialDef = (MaterialDef) loader.load(assetInfo);

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();

        final J3mdExporter materialExporter = new J3mdExporter();
        materialExporter.save(materialDef, bout);

        final String materialDefContent = new String(bout.toByteArray(), "UTF-8");

        final ShaderNodesProject project = new ShaderNodesProject();
        project.setMaterialDefContent(materialDefContent);

        final BinaryExporter exporter = BinaryExporter.getInstance();

        try (final OutputStream out = Files.newOutputStream(resultFile)) {
            exporter.save(project, out);
        }
    }
}
