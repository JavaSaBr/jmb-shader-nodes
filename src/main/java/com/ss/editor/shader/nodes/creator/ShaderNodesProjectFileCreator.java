package com.ss.editor.shader.nodes.creator;

import com.jme3.export.binary.BinaryExporter;
import com.ss.editor.annotation.BackgroundThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.plugin.api.file.creator.GenericFileCreator;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ShaderNodesEditorPlugin;
import com.ss.editor.shader.nodes.model.shader.nodes.ShaderNodesProject;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.VarTable;
import org.jetbrains.annotations.NotNull;

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

    /**
     * The description of this creator.
     */
    @NotNull
    public static final FileCreatorDescription DESCRIPTION = new FileCreatorDescription();

    static {
        DESCRIPTION.setFileDescription(PluginMessages.SNS_CREATOR_DESCRIPTION);
        DESCRIPTION.setConstructor(ShaderNodesProjectFileCreator::new);
    }

    /**
     * The template of material definition.
     */
    @NotNull
    private static final String MD_TEMPLATE;

    static {
        final InputStream mdResource = ShaderNodesProjectFileCreator.class
                .getResourceAsStream("/com/ss/editor/shader/nodes/template/MaterialDefinition.j3md");

        MD_TEMPLATE = FileUtils.read(mdResource);
    }

    @Override
    @FromAnyThread
    protected @NotNull String getTitleText() {
        return PluginMessages.SNS_CREATOR_TITLE;
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

        final ShaderNodesProject project = new ShaderNodesProject();
        project.setMaterialDefContent(MD_TEMPLATE);

        final BinaryExporter exporter = BinaryExporter.getInstance();

        try (final OutputStream out = Files.newOutputStream(resultFile)) {
            exporter.save(project, out);
        }
    }
}
