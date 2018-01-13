package com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add;

import static java.util.stream.Collectors.toList;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderNode;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.JmeThread;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.ShaderNodeOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of adding new shader nodes.
 *
 * @author JavaSaBr
 */
public class AddShaderNodeOperation extends ShaderNodeOperation {

    /**
     * The technique definition.
     */
    @NotNull
    private final TechniqueDef techniqueDef;

    /**
     * The shader nodes.
     */
    @NotNull
    private final ShaderNode shaderNode;

    /**
     * The location.
     */
    @NotNull
    private final Vector2f location;

    /**
     * The previous list of shader nodes.
     */
    @Nullable
    private List<ShaderNode> previousShaderNodes;

    public AddShaderNodeOperation(@NotNull final TechniqueDef techniqueDef, @NotNull final ShaderNode shaderNode,
                                  @NotNull final Vector2f location) {
        this.techniqueDef = techniqueDef;
        this.shaderNode = shaderNode;
        this.location = location;
    }

    @Override
    @JmeThread
    protected void redoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInJMEThread(editor);

        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();
        previousShaderNodes = new ArrayList<>(shaderNodes);

        final List<ShaderNode> vertexes = shaderNodes.stream()
                .filter(node -> node.getDefinition().getType() == Shader.ShaderType.Vertex)
                .collect(toList());

        final List<ShaderNode> fragments = shaderNodes.stream()
                .filter(node -> node.getDefinition().getType() == Shader.ShaderType.Fragment)
                .collect(toList());

        final Shader.ShaderType shaderType = shaderNode.getDefinition().getType();

        if (shaderType == Shader.ShaderType.Fragment) {
            fragments.add(shaderNode);
        } else if (shaderType == Shader.ShaderType.Vertex) {
            vertexes.add(shaderNode);
        }

        shaderNodes.clear();
        shaderNodes.addAll(vertexes);
        shaderNodes.addAll(fragments);
    }

    @Override
    @FxThread
    protected void redoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.redoImplInFXThread(editor);
        editor.notifyAddedShaderNode(shaderNode, location);
    }

    @Override
    @JmeThread
    protected void undoImplInJMEThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInJMEThread(editor);
        final List<ShaderNode> shaderNodes = techniqueDef.getShaderNodes();
        shaderNodes.clear();
        shaderNodes.addAll(previousShaderNodes);
        previousShaderNodes = null;
    }

    @Override
    @FxThread
    protected void undoImplInFXThread(@NotNull final ShaderNodesChangeConsumer editor) {
        super.undoImplInFXThread(editor);
        editor.notifyRemovedRemovedShaderNode(shaderNode);
    }
}
