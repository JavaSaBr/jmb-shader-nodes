package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImport;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImports;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to add an import.
 *
 * @author JavaSaBr
 */
public class AddShaderNodeDefinitionImportOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The shader node imports.
     */
    @NotNull
    private final ShaderNodeDefinitionImports imports;

    /**
     * The shader node import.
     */
    @NotNull
    private final ShaderNodeDefinitionImport anImport;

    public AddShaderNodeDefinitionImportOperation(@NotNull final ShaderNodeDefinitionImports imports,
                                                  @NotNull final ShaderNodeDefinitionImport anImport) {
        this.imports = imports;
        this.anImport = anImport;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        imports.add(anImport);
        editor.notifyFXAddedChild(imports, anImport, -1, true);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        imports.remove(anImport);
        editor.notifyFXRemovedChild(imports, anImport);
    }
}
