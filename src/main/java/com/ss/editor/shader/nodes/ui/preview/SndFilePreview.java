package com.ss.editor.shader.nodes.ui.preview;

import com.jme3.asset.ShaderNodeDefinitionKey;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FxThread;
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

    @Override
    @FxThread
    protected @NotNull SndDocumentationArea createGraphicsNode() {
        var documentationArea = new SndDocumentationArea();
        documentationArea.setEditable(false);
        return documentationArea;
    }

    @Override
    @FxThread
    protected void initialize(@NotNull SndDocumentationArea node, @NotNull StackPane pane) {
        super.initialize(node, pane);
        node.prefWidthProperty().bind(pane.widthProperty());
        node.prefHeightProperty().bind(pane.heightProperty());
    }

    @Override
    @FxThread
    public void show(@NotNull Path file) {
        super.show(file);

        var assetPath = EditorUtil.toAssetPath(file);
        var key = new ShaderNodeDefinitionKey(assetPath);
        key.setLoadDocumentation(true);

        var assetManager = EditorUtil.getAssetManager();
        var definitionList = assetManager.loadAsset(key);

        show(definitionList);
    }

    @FxThread
    private void show(@NotNull List<ShaderNodeDefinition> definitionList) {

        var documentationArea = getGraphicsNode();

        if (definitionList.size() == 1) {

            var documentation = definitionList.get(0).getDocumentation();
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

        var result = new StringBuilder();

        for (var definition : definitionList) {

            var documentation = definition.getDocumentation();
            if (StringUtils.isEmpty(documentation)) {
                continue;
            }

            result.append("// ----- ")
                    .append(definition.getName())
                    .append(" ----- //\n")
                    .append(documentation)
                    .append('\n').append('\n');
        }

        documentationArea.reloadContent(result.toString());
    }

    @Override
    @FxThread
    public void show(@NotNull String resource) {
        super.show(resource);

        var key = new ShaderNodeDefinitionKey(resource);
        key.setLoadDocumentation(true);

        var assetManager = EditorUtil.getAssetManager();
        var definitionList = assetManager.loadAsset(key);

        show(definitionList);
    }

    @Override
    @FxThread
    public boolean isSupport(@NotNull Path file) {
        var extension = FileUtils.getExtension(file);
        return FileExtensions.JME_SHADER_NODE.equals(extension);
    }

    @FxThread
    @Override
    public boolean isSupport(@NotNull String resource) {
        var extension = FileUtils.getExtension(resource);
        return FileExtensions.JME_SHADER_NODE.equals(extension);
    }

    @Override
    @FxThread
    public int getOrder() {
        return 10;
    }
}
