package com.ss.editor.shader.nodes.editor;

import static com.ss.editor.util.EditorUtil.getAssetFile;
import static com.ss.editor.util.EditorUtil.toAssetPath;
import static com.ss.rlib.util.ClassUtils.unsafeCast;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ShaderNodeDefinitionKey;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.editor.BaseFileEditorWithSplitRightTool;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionList;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.editor.ui.component.editor.state.impl.EditorWithEditorToolEditorState;
import com.ss.editor.ui.component.tab.EditorToolComponent;
import com.ss.editor.ui.control.property.PropertyEditor;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * The editor to edit j3sn files.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionFileEditor extends BaseFileEditorWithSplitRightTool<EditorWithEditorToolEditorState> {

    /**
     * The description of this editor.
     */
    @NotNull
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setConstructor(ShaderNodeDefinitionFileEditor::new);
        DESCRIPTION.setEditorName("Shader node definitions editor");
        DESCRIPTION.setEditorId(ShaderNodeDefinitionFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(FileExtensions.JME_SHADER_NODE);
    }

    /**
     * The tree of a structure of opened shader node file.
     */
    @Nullable
    private NodeTree<ChangeConsumer> structureTree;

    /**
     * The property editor.
     */
    @Nullable
    private PropertyEditor<ChangeConsumer> propertyEditor;

    /**
     * THe list of loaded definitions.
     */
    @Nullable
    private List<ShaderNodeDefinition> definitionList;

    @Override
    @FXThread
    protected void createToolComponents(@NotNull final EditorToolComponent container, @NotNull final StackPane root) {
        super.createToolComponents(container, root);

        structureTree = new NodeTree<>(this::selectFromTree, this);
        propertyEditor = new PropertyEditor<>(this);
        propertyEditor.prefHeightProperty().bind(root.heightProperty());

        container.addComponent(buildSplitComponent(structureTree, propertyEditor, root), "Structure");

        FXUtils.addClassTo(structureTree.getTreeView(), CSSClasses.TRANSPARENT_TREE_VIEW);
    }

    @FXThread
    private void selectFromTree(@Nullable final Object object) {

        Object parent = null;
        Object element;

        if (object instanceof TreeNode<?>) {
            final TreeNode treeNode = (TreeNode) object;
            final TreeNode parentNode = treeNode.getParent();
            parent = parentNode == null ? null : parentNode.getElement();
            element = treeNode.getElement();
        } else {
            element = object;
        }

        getPropertyEditor().buildFor(element, parent);
    }

    @Override
    @FXThread
    protected void doOpenFile(@NotNull final Path file) throws IOException {
        super.doOpenFile(file);

        final Path assetFile = notNull(getAssetFile(file));
        final String assetPath = toAssetPath(assetFile);
        final ShaderNodeDefinitionKey key = new ShaderNodeDefinitionKey(assetPath);
        key.setLoadDocumentation(true);

        final AssetManager assetManager = EDITOR.getAssetManager();

        definitionList = assetManager.loadAsset(key);

        getStructureTree().fill(new ShaderNodeDefinitionList(definitionList));
    }

    /**
     * Get the structure tree.
     *
     * @return the structure tree.
     */
    @FXThread
    private @NotNull NodeTree<ChangeConsumer> getStructureTree() {
        return notNull(structureTree);
    }

    /**
     * Get the property editor.
     *
     * @return the property editor.
     */
    @FXThread
    private @NotNull PropertyEditor<ChangeConsumer> getPropertyEditor() {
        return notNull(propertyEditor);
    }

    @Override
    @FXThread
    protected void createEditorAreaPane() {
        super.createEditorAreaPane();
    }

    @Override
    @FXThread
    protected boolean needToolbar() {
        return true;
    }

    @Override
    @FXThread
    protected void createToolbar(@NotNull final HBox container) {
        super.createToolbar(container);
        FXUtils.addToPane(createSaveAction(), container);
    }

    @Override
    @FXThread
    protected @Nullable Supplier<EditorState> getEditorStateFactory() {
        return EditorWithEditorToolEditorState::new;
    }

    @Override
    @FromAnyThread
    public @NotNull EditorDescription getDescription() {
        return DESCRIPTION;
    }
}
