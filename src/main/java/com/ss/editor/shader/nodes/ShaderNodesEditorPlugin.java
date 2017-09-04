package com.ss.editor.shader.nodes;

import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.plugin.EditorPlugin;
import com.ss.editor.shader.nodes.editor.ShaderNodesFileEditor;
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
    public static final String CSS_SHADER_NODES_ROOT = "shader-nodes-root";

    @NotNull
    public static final String CSS_SHADER_NODE = "shader-node";

    @NotNull
    public static final String CSS_SHADER_NODE_HEADER = "header";

    @NotNull
    public static final String CSS_SHADER_NODE_PARAMETER = "shader-node-parameter";

    @NotNull
    public static final String CSS_SHADER_NODE_INPUT_PARAMETER = "shader-node-input-parameter";

    @NotNull
    public static final String CSS_SHADER_NODE_OUTPUT_PARAMETER = "shader-node-output-parameter";

    @NotNull
    public static final String CSS_SHADER_NODE_PARAMETER_SOCKET = "shader-node-parameter-socket";

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
    public void register(@NotNull final EditorRegistry registry) {
        super.register(registry);
        registry.register(ShaderNodesFileEditor.DESCRIPTION);
    }
}
