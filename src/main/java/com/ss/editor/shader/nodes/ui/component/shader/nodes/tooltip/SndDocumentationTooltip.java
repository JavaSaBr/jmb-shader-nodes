package com.ss.editor.shader.nodes.ui.component.shader.nodes.tooltip;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ShaderNodeDefinitionKey;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.ui.PluginCssClasses;
import com.ss.editor.shader.nodes.ui.component.SndDocumentationArea;
import com.ss.editor.ui.tooltip.CustomTooltip;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The tooltip with documentation of shader node definition.
 *
 * @author JavaSaBr
 */
public class SndDocumentationTooltip extends CustomTooltip<BorderPane> {

    @NotNull
    private final ShaderNodeDefinition definition;

    @Nullable
    private SndDocumentationArea documentation;

    public SndDocumentationTooltip(@NotNull final ShaderNodeDefinition definition) {
        this.definition = definition;
    }

    @Override
    @FxThread
    protected @NotNull BorderPane createRoot() {
        return new BorderPane();
    }

    @Override
    @FxThread
    protected void createContent(@NotNull final BorderPane root) {
        super.createContent(root);
        this.documentation = new SndDocumentationArea();
        this.documentation.setEditable(false);
        root.setCenter(documentation);
        FXUtils.addClassesTo(documentation, PluginCssClasses.SHADER_NODE_DEF_DOCUMENTATION_TOOLTIP);
    }

    /**
     * Get the definition.
     *
     * @return the definition.
     */
    @FxThread
    private @NotNull ShaderNodeDefinition getDefinition() {
        return definition;
    }

    /**
     * Get the area to show documentation.
     *
     * @return the area to show documentation.
     */
    @FxThread
    private @NotNull SndDocumentationArea getDocumentation() {
        return notNull(documentation);
    }

    @Override
    @FxThread
    protected void show() {

        final ShaderNodeDefinition definition = getDefinition();
        final ShaderNodeDefinitionKey assetKey = new ShaderNodeDefinitionKey(definition.getPath());
        assetKey.setLoadDocumentation(true);

        final AssetManager assetManager = EditorUtil.getAssetManager();
        String documentation = assetManager.loadAsset(assetKey).stream()
                .filter(def -> def.getName().equals(definition.getName()))
                .map(ShaderNodeDefinition::getDocumentation)
                .findAny().orElse("");

        if (!StringUtils.isEmpty(documentation)) {
            getDocumentation().reloadContent(documentation.trim());
        }

        super.show();
    }
}
