package com.ss.editor.shader.nodes.ui.preview;

import com.jme3.asset.AssetManager;
import com.jme3.asset.ShaderNodeDefinitionKey;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.Editor;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.ui.component.SndDocumentationArea;
import com.ss.editor.ui.preview.impl.AbstractFilePreview;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.StringUtils;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * The implementation of preview of shader node definition files.
 *
 * @author JavaSaBr
 */
public class SndFilePreview extends AbstractFilePreview<SndDocumentationArea> {

    @NotNull
    private static final Editor EDITOR = Editor.getInstance();

    @Override
    @FXThread
    protected @NotNull SndDocumentationArea createGraphicsNode() {
        final SndDocumentationArea documentationArea = new SndDocumentationArea();
        documentationArea.setEditable(false);
        return documentationArea;
    }

    @Override
    @FXThread
    protected void initialize(@NotNull final SndDocumentationArea node, @NotNull final StackPane pane) {
        super.initialize(node, pane);
        node.prefWidthProperty().bind(pane.widthProperty());
        node.prefHeightProperty().bind(pane.heightProperty());
    }

    @Override
    @FXThread
    public void show(@NotNull final Path file) {
        super.show(file);

        final String assetPath = EditorUtil.toAssetPath(file);
        final ShaderNodeDefinitionKey key = new ShaderNodeDefinitionKey(assetPath);
        key.setLoadDocumentation(true);

        final AssetManager assetManager = EDITOR.getAssetManager();
        final List<ShaderNodeDefinition> definitionList = assetManager.loadAsset(key);

        show(definitionList);
    }

    @FXThread
    private void show(@NotNull final List<ShaderNodeDefinition> definitionList) {

        final SndDocumentationArea documentationArea = getGraphicsNode();

        if (definitionList.size() == 1) {

            String documentation = definitionList.get(0).getDocumentation();
            if(StringUtils.isEmpty(documentation)) {
                documentationArea.reloadContent(" ");
                return;
            }

            if (documentation.startsWith("\n")) {
                documentation = documentation.substring(1, documentation.length());
            }

            documentationArea.reloadContent(documentation);
            return;
        }

        final StringBuilder result = new StringBuilder();

        for (final ShaderNodeDefinition definition : definitionList) {

            final String documentation = definition.getDocumentation();
            if (StringUtils.isEmpty(documentation)) continue;

            result.append("// ----- ")
                    .append(definition.getName())
                    .append(" ----- //\n")
                    .append(documentation)
                    .append('\n').append('\n');
        }

        documentationArea.reloadContent(result.toString());
    }

    @Override
    @FXThread
    public void show(@NotNull final String resource) {
        super.show(resource);

        final ShaderNodeDefinitionKey key = new ShaderNodeDefinitionKey(resource);
        key.setLoadDocumentation(true);

        final AssetManager assetManager = EDITOR.getAssetManager();
        final List<ShaderNodeDefinition> definitionList = assetManager.loadAsset(key);

        show(definitionList);
    }

    @Override
    @FXThread
    public boolean isSupport(@NotNull final Path file) {
        final String extension = FileUtils.getExtension(file);
        return FileExtensions.JME_SHADER_NODE.equals(extension);
    }

    @FXThread
    @Override
    public boolean isSupport(@NotNull final String resource) {
        final String extension = FileUtils.getExtension(resource);
        return FileExtensions.JME_SHADER_NODE.equals(extension);
    }

    @Override
    @FXThread
    public int getOrder() {
        return 10;
    }
}
