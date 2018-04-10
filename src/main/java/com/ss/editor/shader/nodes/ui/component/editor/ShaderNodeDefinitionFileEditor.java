package com.ss.editor.shader.nodes.ui.component.editor;

import static com.ss.editor.util.EditorUtil.*;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ShaderNodeDefinitionKey;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.glsl.parser.GlslParser;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.BackgroundThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.extension.property.EditableProperty;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.editor.BaseFileEditorWithSplitRightTool;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndList;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndShaderSource;
import com.ss.editor.shader.nodes.ui.component.editor.state.ShaderNodeDefinitionEditorState;
import com.ss.editor.shader.nodes.util.J3snExporter;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.editor.ui.component.tab.EditorToolComponent;
import com.ss.editor.ui.control.code.GLSLCodeArea;
import com.ss.editor.ui.control.property.PropertyEditor;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.ui.css.CssClasses;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.dictionary.DictionaryFactory;
import com.ss.rlib.util.dictionary.ObjectDictionary;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * The editor to edit j3sn files.
 *
 * @author JavaSaBr
 */
public class ShaderNodeDefinitionFileEditor extends BaseFileEditorWithSplitRightTool<ShaderNodeDefinitionEditorState> {

    /**
     * The description of this editor.
     */
    @NotNull
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setConstructor(ShaderNodeDefinitionFileEditor::new);
        DESCRIPTION.setEditorName(PluginMessages.SND_EDITOR_NAME);
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

    /**
     * The flag to ignore GLSL code changes.
     */
    private boolean ignoreCodeChanges;

    public ShaderNodeDefinitionFileEditor() {
        this.glslChangedContent = DictionaryFactory.newObjectDictionary();
        this.glslOriginalContent = DictionaryFactory.newObjectDictionary();
    }

    @Override
    @FxThread
    protected void createToolComponents(@NotNull EditorToolComponent container, @NotNull StackPane root) {
        super.createToolComponents(container, root);

        structureTree = new NodeTree<>(this::selectFromTree, this, SelectionMode.SINGLE);
        propertyEditor = new PropertyEditor<>(this);
        propertyEditor.prefHeightProperty().bind(root.heightProperty());

        container.addComponent(buildSplitComponent(structureTree, propertyEditor, root),
                PluginMessages.SND_EDITOR_TOOL_STRUCTURE);

        FXUtils.addClassTo(structureTree.getTreeView(), CssClasses.TRANSPARENT_TREE_VIEW);
    }

    @FxThread
    @Override
    protected void calcVSplitSize(@NotNull SplitPane splitPane) {
        splitPane.setDividerPosition(0, 0.6);
    }

    @Override
    @FxThread
    protected void createEditorAreaPane() {
        super.createEditorAreaPane();

        var editorAreaPane = getEditorAreaPane();

        codeArea = new GLSLCodeArea();
        codeArea.loadContent("");
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> changeGLSLCode(newValue));

        FXUtils.addToPane(codeArea, editorAreaPane);
    }

    /**
     * Get the code area.
     *
     * @return the code area.
     */
    @FxThread
    private @NotNull GLSLCodeArea getCodeArea() {
        return notNull(codeArea);
    }

    /**
     * Handle selected objects from the structure tree.
     *
     * @param objects the selected objects.
     */
    @FxThread
    private void selectFromTree(@Nullable Array<Object> objects) {
        setEditedShader(null);

        var codeArea = getCodeArea();
        codeArea.setEditable(false);

        var object = objects == null? null : objects.first();

        Object parent = null;
        Object element;

        if (object instanceof TreeNode<?>) {
            var treeNode = (TreeNode) object;
            var parentNode = treeNode.getParent();
            parent = parentNode == null ? null : parentNode.getElement();
            element = treeNode.getElement();
        } else {
            element = object;
        }

        if (element instanceof SndShaderSource) {

            var shaderSource = (SndShaderSource) element;
            var code = getGlslCode(shaderSource.getShaderPath());

           /* EXECUTOR_MANAGER.schedule(() -> {
                try {
                    //FIXME to delete
                    GlslParser.newInstance().parseFileDeclaration(shaderSource.getShaderPath(), code);
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }, 1000);*/

            setEditedShader(shaderSource.getShaderPath());
            setIgnoreCodeChanges(true);
            try {
                codeArea.reloadContent(code, true);
                codeArea.setEditable(true);
            } finally {
                setIgnoreCodeChanges(false);
            }
        }

        getPropertyEditor().buildFor(element, parent);
    }

    /**
     * Get the dictionary to store original GLSL code.
     *
     * @return the dictionary to store original GLSL code.
     */
    @FxThread
    private @NotNull ObjectDictionary<String, String> getGlslOriginalContent() {
        return glslOriginalContent;
    }

    /**
     * Get the dictionary to track changes of GLSL code.
     *
     * @return the dictionary to track changes of GLSL code.
     */
    @FxThread
    private @NotNull ObjectDictionary<String, String> getGlslChangedContent() {
        return glslChangedContent;
    }

    /**
     * Get the current edited shader.
     *
     * @return the current edited shader.
     */
    @FxThread
    private @Nullable String getEditedShader() {
        return editedShader;
    }

    /**
     * Set the current edited shader.
     *
     * @param editedShader the current edited shader.
     */
    @FxThread
    private void setEditedShader(@Nullable String editedShader) {
        this.editedShader = editedShader;
    }

    /**
     * Return true if need to ignore GLSL code changes.
     *
     * @return true if need to ignore GLSL code changes.
     */
    @FxThread
    private boolean isIgnoreCodeChanges() {
        return ignoreCodeChanges;
    }

    /**
     * Set true if need to ignore GLSL code changes.
     *
     * @param ignoreCodeChanges true if need to ignore GLSL code changes.
     */
    @FxThread
    private void setIgnoreCodeChanges(boolean ignoreCodeChanges) {
        this.ignoreCodeChanges = ignoreCodeChanges;
    }

    /**
     * Handle changes of GLSL code.
     *
     * @param glslCode the new GLSL code.
     */
    @FxThread
    private void changeGLSLCode(@NotNull String glslCode) {

        if (isIgnoreCodeChanges()) {
            return;
        }

        var editedShader = getEditedShader();
        if (editedShader == null) {
            return;
        }

        var glslOriginalContent = getGlslOriginalContent();
        var originalCode = glslOriginalContent.get(editedShader);

        if (!glslCode.equals(originalCode)) {
            incrementChange();
        }

        var glslChangedContent = getGlslChangedContent();
        glslChangedContent.put(editedShader, glslCode);
    }

    /**
     * Get GLSL code of the shader by the path.
     *
     * @param shaderPath the shader path.
     * @return the GLSL code.
     */
    @FxThread
    private @NotNull String getGlslCode(@NotNull String shaderPath) {

        var glslChangedContent = getGlslChangedContent();
        var glslCode = glslChangedContent.get(shaderPath);

        if (glslCode != null) {
            return glslCode;
        }

        var glslOriginalContent = getGlslOriginalContent();
        var realFile = notNull(getRealFile(shaderPath));

        var readGLSLCode = FileUtils.read(realFile);

        glslOriginalContent.put(shaderPath, readGLSLCode);

        if (readGLSLCode.isEmpty()) {
            glslChangedContent.put(shaderPath, "void main() {\n\n}");
            return "void main() {\n\n}";
        }

        glslChangedContent.put(shaderPath, readGLSLCode);

        return readGLSLCode;
    }

    @Override
    @FxThread
    protected void doOpenFile(@NotNull Path file) throws IOException {
        super.doOpenFile(file);

        var assetFile = notNull(getAssetFile(file));
        var assetPath = toAssetPath(assetFile);

        var key = new ShaderNodeDefinitionKey(assetPath);
        key.setLoadDocumentation(true);

        var assetManager = EditorUtil.getAssetManager();

        definitionList = assetManager.loadAsset(key);

        var structureTree = getStructureTree();
        structureTree.fill(new SndList(definitionList));
        structureTree.expandToLevel(1);
    }

    /**
     * Get the definitions list.
     *
     * @return the definitions list.
     */
    @FromAnyThread
    private @NotNull List<ShaderNodeDefinition> getDefinitionList() {
        return notNull(definitionList);
    }

    @Override
    @BackgroundThread
    protected void doSave(@NotNull Path toStore) throws IOException {
        super.doSave(toStore);

        var exporter = J3snExporter.getInstance();

        try (var out = Files.newOutputStream(toStore)) {
            exporter.export(getDefinitionList(), out);
        }

        var glslChangedContent = getGlslChangedContent();
        var glslOriginalContent = getGlslOriginalContent();

        glslChangedContent.forEach((path, content) -> {

            var original = glslOriginalContent.get(path);

            if (StringUtils.equals(content, original)) {
                return;
            }

            var realFile = notNull(getRealFile(path));

            try (var out = new PrintStream(Files.newOutputStream(realFile))) {
                out.print(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            glslOriginalContent.put(path, content);
        });
    }

    /**
     * Get the structure tree.
     *
     * @return the structure tree.
     */
    @FxThread
    private @NotNull NodeTree<ChangeConsumer> getStructureTree() {
        return notNull(structureTree);
    }

    /**
     * Get the property editor.
     *
     * @return the property editor.
     */
    @FxThread
    private @NotNull PropertyEditor<ChangeConsumer> getPropertyEditor() {
        return notNull(propertyEditor);
    }

    @Override
    @FxThread
    protected boolean needToolbar() {
        return true;
    }

    @Override
    @FxThread
    protected void createToolbar(@NotNull HBox container) {
        super.createToolbar(container);
        FXUtils.addToPane(createSaveAction(), container);
    }

    @Override
    @FxThread
    protected @Nullable Supplier<EditorState> getEditorStateFactory() {
        return ShaderNodeDefinitionEditorState::new;
    }

    @Override
    @FromAnyThread
    public @NotNull EditorDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    @FxThread
    public void notifyFxAddedChild(
        @NotNull Object parent,
        @NotNull Object added,
        int index,
        boolean needSelect
    ) {

        var structureTree = getStructureTree();
        structureTree.notifyAdded(parent, added, index);

        if (needSelect) {
            structureTree.selectSingle(added);
        }
    }

    @Override
    @FxThread
    public void notifyFxRemovedChild(@NotNull Object parent, @NotNull Object removed) {
        getStructureTree().notifyRemoved(parent, removed);
    }

    @Override
    @FxThread
    public void notifyFxChangeProperty(
        @Nullable Object parent,
        @NotNull Object object,
        @NotNull String propertyName
    ) {
        super.notifyFxChangeProperty(parent, object, propertyName);

        var structureTree = getStructureTree();
        structureTree.notifyChanged(parent, object);

        if (object instanceof EditableProperty) {
            getPropertyEditor().refresh();
        }
    }
}
