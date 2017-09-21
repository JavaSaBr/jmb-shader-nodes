package com.ss.editor.shader.nodes.editor;

import static com.ss.editor.util.EditorUtil.*;
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
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeShaderSource;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.editor.ui.component.editor.state.impl.EditorWithEditorToolEditorState;
import com.ss.editor.ui.component.tab.EditorToolComponent;
import com.ss.editor.ui.control.code.GLSLCodeArea;
import com.ss.editor.ui.control.property.PropertyEditor;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.dictionary.DictionaryFactory;
import com.ss.rlib.util.dictionary.ObjectDictionary;
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
     * The dictionary to track changes of GLSL code.
     */
    @NotNull
    private final ObjectDictionary<String, String> glslChangedContent;

    /**
     * The dictionary to store original GLSL code.
     */
    @NotNull
    private final ObjectDictionary<String, String> glslOriginalContent;

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
     * The code area to show GLSL code.
     */
    @Nullable
    private GLSLCodeArea codeArea;

    /**
     * Current edited shader.
     */
    @Nullable
    private String editedShader;

    /**
     * The list of loaded definitions.
     */
    @Nullable
    private List<ShaderNodeDefinition> definitionList;

    public ShaderNodeDefinitionFileEditor() {
        this.glslChangedContent = DictionaryFactory.newObjectDictionary();
        this.glslOriginalContent = DictionaryFactory.newObjectDictionary();
    }

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

    @Override
    @FXThread
    protected void createEditorAreaPane() {
        super.createEditorAreaPane();

        final StackPane editorAreaPane = getEditorAreaPane();

        codeArea = new GLSLCodeArea();
        codeArea.loadContent("");

        FXUtils.addToPane(codeArea, editorAreaPane);
    }

    /**
     * Get the code area.
     *
     * @return the code area.
     */
    @FXThread
    private @NotNull GLSLCodeArea getCodeArea() {
        return notNull(codeArea);
    }

    @FXThread
    private void selectFromTree(@Nullable final Object object) {

        final GLSLCodeArea codeArea = getCodeArea();
        codeArea.setEditable(false);

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

        if (element instanceof ShaderNodeShaderSource) {

            final ShaderNodeShaderSource shaderSource = (ShaderNodeShaderSource) element;
            final String code = getGLSLCode(shaderSource.getShaderPath());
            codeArea.reloadContent(code);

            editedShader = shaderSource.getShaderPath();
        }

        getPropertyEditor().buildFor(element, parent);
    }

    /**
     * Get the dictionary to store original GLSL code.
     *
     * @return the dictionary to store original GLSL code.
     */
    @FXThread
    private @NotNull ObjectDictionary<String, String> getGlslOriginalContent() {
        return glslOriginalContent;
    }

    /**
     * Get the dictionary to track changes of GLSL code.
     *
     * @return the dictionary to track changes of GLSL code.
     */
    @FXThread
    private @NotNull ObjectDictionary<String, String> getGlslChangedContent() {
        return glslChangedContent;
    }

    /**
     * Get GLSL code of the shader by the path.
     *
     * @param shaderPath the shader path.
     * @return the GLSL code.
     */
    @FXThread
    private @NotNull String getGLSLCode(@NotNull final String shaderPath) {

        final ObjectDictionary<String, String> glslChangedContent = getGlslChangedContent();
        final String glslCode = glslOriginalContent.get(shaderPath);

        if (glslCode != null) {
            return glslCode;
        }

        final ObjectDictionary<String, String> glslOriginalContent = getGlslOriginalContent();
        final Path realFile = notNull(getRealFile(shaderPath));

        final String readGLSLCode = FileUtils.read(realFile);

        glslChangedContent.put(shaderPath, readGLSLCode);
        glslOriginalContent.put(shaderPath, readGLSLCode);

        return readGLSLCode;
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

    @Override
    @FXThread
    public void notifyFXAddedChild(@NotNull final Object parent, @NotNull final Object added, final int index,
                                   final boolean needSelect) {

        final NodeTree<ChangeConsumer> structureTree = getStructureTree();
        structureTree.notifyAdded(parent, added, index);

        if (needSelect) {
            structureTree.select(added);
        }
    }

    @Override
    @FXThread
    public void notifyFXRemovedChild(@NotNull final Object parent, @NotNull final Object removed) {
        getStructureTree().notifyRemoved(parent, removed);
    }
}
