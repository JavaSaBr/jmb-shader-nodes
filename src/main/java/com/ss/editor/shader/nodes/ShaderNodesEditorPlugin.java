package com.ss.editor.shader.nodes;

import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.manager.FileIconManager;
import com.ss.editor.plugin.EditorPlugin;
import com.ss.editor.shader.nodes.creator.ShaderNodesProjectFileCreator;
import com.ss.editor.shader.nodes.editor.ShaderNodesFileEditor;
import com.ss.editor.ui.component.creator.FileCreatorRegistry;
import com.ss.editor.ui.component.editor.EditorRegistry;
import com.ss.editor.ui.css.CSSRegistry;
import com.ss.rlib.plugin.PluginContainer;
import com.ss.rlib.plugin.annotation.PluginDescription;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of an editor plugin.
 *
 * @author JavaSaBr
 */
@PluginDescription(
        id = "com.ss.editor.shader.nodes",
        version = "1.0.0",
        minAppVersion = "1.1.0",
        name = "Shader Nodes",
        description = "A plugin with an Editor to work with Shader Nodes."
)
public class ShaderNodesEditorPlugin extends EditorPlugin {

    @NotNull
    public static final String PROJECT_FILE_EXTENSION = "j3snm";

    public ShaderNodesEditorPlugin(@NotNull final PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final CSSRegistry registry) {
        super.register(registry);
        registry.register("com/ss/editor/shader/nodes/style.css");
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final FileCreatorRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesProjectFileCreator.DESCRIPTION);
    }

    @Override
    @FromAnyThread
    public void register(@NotNull final EditorRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesFileEditor.DESCRIPTION);
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
}
