package com.ss.editor.shader.nodes.tree.operation;

import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImport;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionImports;
import org.jetbrains.annotations.NotNull;

/**
 * The operation to delete an import.
 *
 * @author JavaSaBr
 */
public class DeleteShaderNodeDefinitionImportOperation extends AbstractEditorOperation<ChangeConsumer> {

    /**
     * The shader node definition imports.
     */
    @NotNull
    private final ShaderNodeDefinitionImports imports;

    /**
     * The import.
     */
    @NotNull
    private final ShaderNodeDefinitionImport anImport;

    /**
     * The previous position.
     */
    private int index;

    public DeleteShaderNodeDefinitionImportOperation(@NotNull final ShaderNodeDefinitionImports imports,
                                                     @NotNull final ShaderNodeDefinitionImport anImport) {
        this.imports = imports;
        this.anImport = anImport;
    }

    @Override
    @FXThread
    protected void redoImpl(@NotNull final ChangeConsumer editor) {
        index = imports.indexOf(anImport);
        imports.remove(anImport);
        editor.notifyFXRemovedChild(imports, anImport);
    }

    @Override
    @FXThread
    protected void undoImpl(@NotNull final ChangeConsumer editor) {
        imports.add(index, anImport);
        editor.notifyFXAddedChild(imports, anImport, index, false);
    }
}
