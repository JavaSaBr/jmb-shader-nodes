package com.ss.editor.shader.nodes;

import com.jme3.asset.AssetManager;
import com.jme3.shader.glsl.AstGlsl150ShaderGenerator;
import com.jme3.shader.glsl.AstShaderGenerator;
import com.ss.editor.Editor;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.manager.FileIconManager;
import com.ss.editor.manager.ResourceManager;
import com.ss.editor.plugin.EditorPlugin;
import com.ss.editor.shader.nodes.ui.component.creator.ShaderNodeDefinitionsFileCreator;
import com.ss.editor.shader.nodes.ui.component.creator.ShaderNodesProjectFileCreator;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodeDefinitionFileEditor;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesFileEditor;
import com.ss.editor.shader.nodes.ui.control.tree.factory.ShaderNodesTreeNodeFactory;
import com.ss.editor.shader.nodes.ui.control.tree.property.ShaderNodesPropertyBuilder;
import com.ss.editor.shader.nodes.ui.preview.SndFilePreviewFactory;
import com.ss.editor.ui.component.creator.FileCreatorRegistry;
import com.ss.editor.ui.component.editor.EditorRegistry;
import com.ss.editor.ui.control.property.builder.PropertyBuilderRegistry;
import com.ss.editor.ui.control.tree.node.TreeNodeFactoryRegistry;
import com.ss.editor.ui.css.CSSRegistry;
import com.ss.editor.ui.preview.FilePreviewFactoryRegistry;
import com.ss.rlib.plugin.PluginContainer;
import com.ss.rlib.plugin.PluginSystem;
import com.ss.rlib.plugin.annotation.PluginDescription;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of an editor plugin.
 *
 * @author JavaSaBr
 */
@PluginDescription(
        id = "com.ss.editor.shader.nodes",
        version = "1.1.0",
        minAppVersion = "1.3.2",
        name = "Shader Nodes Tools",
        description = "The plugin with editors to work with shader node materials."
)
public class ShaderNodesEditorPlugin extends EditorPlugin {

    @NotNull
    public static final String PROJECT_FILE_EXTENSION = "j3snm";

    public ShaderNodesEditorPlugin(@NotNull final PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    @Override
    public void onAfterCreateJMEContext(@NotNull final PluginSystem pluginSystem) {
        super.onAfterCreateJMEContext(pluginSystem);
        System.setProperty(AstShaderGenerator.PROP_USE_CASE, "false");
        final AssetManager assetManager = Editor.getInstance().getAssetManager();
        assetManager.setShaderGenerator(new AstGlsl150ShaderGenerator(assetManager));
    }

    @FXThread
    @Override
    public void onBeforeCreateJavaFXContext(@NotNull final PluginSystem pluginSystem) {
        super.onBeforeCreateJavaFXContext(pluginSystem);
        final ResourceManager resourceManager = ResourceManager.getInstance();
        resourceManager.registerInterestedFileType(FileExtensions.JME_SHADER_NODE);
        resourceManager.registerInterestedFileType(FileExtensions.GLSL_LIB);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final CSSRegistry registry) {
        super.register(registry);
        registry.register("com/ss/editor/shader/nodes/style.css", getClassLoader());
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final FileCreatorRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesProjectFileCreator.DESCRIPTION);
        registry.register(ShaderNodeDefinitionsFileCreator.DESCRIPTION);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final EditorRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesFileEditor.DESCRIPTION);
        registry.register(ShaderNodeDefinitionFileEditor.DESCRIPTION);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final TreeNodeFactoryRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesTreeNodeFactory.getInstance());
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final PropertyBuilderRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesPropertyBuilder.getInstance());
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final FileIconManager iconManager) {
        super.register(iconManager);
        iconManager.register((path, extension) -> {
            if (PROJECT_FILE_EXTENSION.equals(extension)) {
                return "com/ss/editor/shader/nodes/icons/vector.svg";
            } else {
                return null;
            }
        });
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final FilePreviewFactoryRegistry registry) {
        super.register(registry);
        registry.register(SndFilePreviewFactory.getInstance());
    }
}
