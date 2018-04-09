package com.ss.editor.shader.nodes;

import com.jme3.shader.glsl.AstGlsl150ShaderGenerator;
import com.jme3.shader.glsl.AstShaderGenerator;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.FxThread;
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
import com.ss.editor.ui.control.tree.node.factory.TreeNodeFactoryRegistry;
import com.ss.editor.ui.css.CssRegistry;
import com.ss.editor.ui.preview.FilePreviewFactoryRegistry;
import com.ss.editor.util.EditorUtil;
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
        version = "1.1.1",
        minAppVersion = "1.7.0",
        name = "Shader Nodes Tools",
        description = "The plugin with editors to work with shader node materials."
)
public class ShaderNodesEditorPlugin extends EditorPlugin {

    @NotNull
    public static final String PROJECT_FILE_EXTENSION = "j3snm";

    public ShaderNodesEditorPlugin(@NotNull PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    @Override
    public void onAfterCreateJmeContext(@NotNull PluginSystem pluginSystem) {
        super.onAfterCreateJmeContext(pluginSystem);
        System.setProperty(AstShaderGenerator.PROP_USE_CASE, "false");
        var assetManager = EditorUtil.getAssetManager();
        assetManager.setShaderGenerator(new AstGlsl150ShaderGenerator(assetManager));
    }

    @FxThread
    @Override
    public void onBeforeCreateJavaFxContext(@NotNull PluginSystem pluginSystem) {
        super.onBeforeCreateJavaFxContext(pluginSystem);
        var resourceManager = ResourceManager.getInstance();
        resourceManager.registerInterestedFileType(FileExtensions.JME_SHADER_NODE);
        resourceManager.registerInterestedFileType(FileExtensions.GLSL_LIB);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull CssRegistry registry) {
        super.register(registry);
        registry.register("com/ss/editor/shader/nodes/style.css", getClassLoader());
    }

    @Override
    @FromAnyThread
    public void register(@NotNull FileCreatorRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesProjectFileCreator.DESCRIPTION);
        registry.register(ShaderNodeDefinitionsFileCreator.DESCRIPTION);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull EditorRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesFileEditor.DESCRIPTION);
        registry.register(ShaderNodeDefinitionFileEditor.DESCRIPTION);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull TreeNodeFactoryRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesTreeNodeFactory.getInstance());
    }

    @Override
    @FromAnyThread
    public void register(@NotNull PropertyBuilderRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesPropertyBuilder.getInstance());
    }

    @Override
    @FromAnyThread
    public void register(@NotNull FileIconManager iconManager) {
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
    public void register(@NotNull FilePreviewFactoryRegistry registry) {
        super.register(registry);
        registry.register(SndFilePreviewFactory.getInstance());
    }
}
