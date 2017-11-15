package com.ss.editor.shader.nodes.ui.dialog;

import static com.ss.editor.ui.util.UIUtils.consumeIf;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.tree.operation.ChangeSndDocumentationOperation;
import com.ss.editor.shader.nodes.ui.PluginCSSClasses;
import com.ss.editor.shader.nodes.ui.component.SndDocumentationArea;
import com.ss.editor.ui.dialog.AbstractSimpleEditorDialog;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * The implementation of a dialog to edit documentation of a shader node definition.
 *
 * @author JavaSaBr
 */
public class EditSndDocumentationDialog extends AbstractSimpleEditorDialog {

    @NotNull
    private static final Point DIALOG_SIZE = new Point(-1, -1);

    /**
     * The change consumer.
     */
    @NotNull
    private final ChangeConsumer consumer;

    /**
     * THe shader node definition.
     */
    @NotNull
    private final ShaderNodeDefinition definition;

    /**
     * The editor area.
     */
    @Nullable
    private SndDocumentationArea editorArea;

    public EditSndDocumentationDialog(@NotNull final ChangeConsumer consumer,
                                      @NotNull final ShaderNodeDefinition definition) {
        this.consumer = consumer;
        this.definition = definition;
        final String documentation = definition.getDocumentation();
        getEditorArea().loadContent(documentation == null ? "" : documentation);
    }

    /**
     * Get the editor area.
     *
     * @return the editor area.
     */
    @FXThread
    private @NotNull SndDocumentationArea getEditorArea() {
        return notNull(editorArea);
    }

    @Override
    @FXThread
    protected void createContent(@NotNull final VBox root) {
        super.createContent(root);

        editorArea = new SndDocumentationArea();
        editorArea.setOnKeyReleased(event -> consumeIf(event,
                keyEvent -> keyEvent.getCode() == KeyCode.ENTER));

        FXUtils.addClassTo(editorArea, PluginCSSClasses.SHADER_NODE_DEF_DOCUMENTATION_DIALOG);
        FXUtils.addToPane(editorArea, root);
    }

    @Override
    @FromAnyThread
    protected @NotNull String getButtonOkText() {
        return Messages.SIMPLE_DIALOG_BUTTON_SAVE;
    }

    @Override
    @FromAnyThread
    protected @NotNull String getTitleText() {
        return PluginMessages.SND_EDITOR_DOCUMENT_DIALOG;
    }

    @Override
    @FXThread
    protected void processOk() {
        super.processOk();

        final String documentation = definition.getDocumentation();
        final String oldVersion = documentation == null? "" : documentation;
        final String newVersion = getEditorArea().getText();

        consumer.execute(new ChangeSndDocumentationOperation(definition, oldVersion, newVersion));
    }

    @Override
    @FromAnyThread
    protected @NotNull Point getSize() {
        return DIALOG_SIZE;
    }
}
