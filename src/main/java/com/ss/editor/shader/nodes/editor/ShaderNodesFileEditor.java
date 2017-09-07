package com.ss.editor.shader.nodes.editor;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.VariableMapping;
import com.jme3.util.clone.Cloner;
import com.ss.editor.annotation.BackgroundThread;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.node.material.RootMaterialSettings;
import com.ss.editor.plugin.api.editor.material.BaseMaterialFileEditor;
import com.ss.editor.shader.nodes.ShaderNodesEditorPlugin;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.model.ShaderNodesProject;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.util.MaterialUtils;
import com.ss.rlib.ui.util.FXUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * The implementation of an editor to work with shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodesFileEditor extends
        BaseMaterialFileEditor<ShaderNodesEditor3DState, ShaderNodesEditorState, ShaderNodesChangeConsumer> implements
        ShaderNodesChangeConsumer {

    /**
     * The description of this editor.
     */
    @NotNull
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setConstructor(ShaderNodesFileEditor::new);
        DESCRIPTION.setEditorName("Shader Nodes Editor");
        DESCRIPTION.setEditorId(ShaderNodesFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(ShaderNodesEditorPlugin.PROJECT_FILE_EXTENSION);
    }

    /**
     * The area to place shader nodes.
     */
    @Nullable
    private BorderPane shaderNodesArea;

    /**
     * The shader nodes container.
     */
    @Nullable
    private ShaderNodesContainer shaderNodesContainer;

    /**
     * The project file.
     */
    @Nullable
    private ShaderNodesProject project;

    /**
     * The list of available techniques.
     */
    @Nullable
    private ComboBox<String> techniqueComboBox;

    @Override
    @FXThread
    protected @NotNull ShaderNodesEditor3DState create3DEditorState() {
        return new ShaderNodesEditor3DState(this);
    }

    @Override
    protected @Nullable Supplier<EditorState> getEditorStateFactory() {
        return ShaderNodesEditorState::new;
    }

    @Override
    @FXThread
    protected void doOpenFile(@NotNull final Path file) throws IOException {
        super.doOpenFile(file);

        final BinaryImporter importer = BinaryImporter.getInstance();
        importer.setAssetManager(EDITOR.getAssetManager());

        try (final InputStream in = Files.newInputStream(file)) {
            setProject((ShaderNodesProject) importer.load(in));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final ShaderNodesEditor3DState editor3DState = getEditor3DState();
        editor3DState.changeMode(ShaderNodesEditor3DState.ModelType.BOX);

        buildMaterial();
    }

    @Override
    @BackgroundThread
    protected void doSave(@NotNull final Path toStore) throws IOException {
        super.doSave(toStore);

        final BinaryExporter exporter = BinaryExporter.getInstance();

        try (final OutputStream out = Files.newOutputStream(toStore)) {
            exporter.save(getProject(), out);
        }
    }

    @Override
    protected void createEditorAreaPane() {
        super.createEditorAreaPane();

        final StackPane editorAreaPane = getEditorAreaPane();
        final BorderPane editor3DArea = notNull(get3DArea());

        shaderNodesContainer = new ShaderNodesContainer(this);
        shaderNodesArea = new BorderPane(shaderNodesContainer);

        FXUtils.removeFromParent(editor3DArea, editorAreaPane);

        final SplitPane splitPanel = new SplitPane(shaderNodesArea, editor3DArea);
        splitPanel.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            splitPanel.setDividerPosition(0, 0.7);
        });

        FXUtils.addToPane(splitPanel, editorAreaPane);
        FXUtils.addClassTo(splitPanel, CSSClasses.FILE_EDITOR_TOOL_SPLIT_PANE);
    }

    /**
     * @return The list of available techniques.
     */
    @FromAnyThread
    private @NotNull ComboBox<String> getTechniqueComboBox() {
        return notNull(techniqueComboBox);
    }

    /**
     * @return the shader nodes container.
     */
    @FromAnyThread
    private @NotNull ShaderNodesContainer getShaderNodesContainer() {
        return notNull(shaderNodesContainer);
    }

    @FXThread
    @Override
    protected void createToolbar(@NotNull final HBox container) {
        super.createToolbar(container);

        final Label techniqueLabel = new Label("Technique:");

        techniqueComboBox = new ComboBox<>();
        techniqueComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> changeTechnique(newValue));

        FXUtils.addToPane(techniqueLabel, container);
        FXUtils.addToPane(techniqueComboBox, container);

        FXUtils.addClassTo(techniqueLabel, CSSClasses.FILE_EDITOR_TOOLBAR_LABEL);
        FXUtils.addClassTo(techniqueComboBox, CSSClasses.FILE_EDITOR_TOOLBAR_FIELD);
    }

    /**
     * Handle changing the technique.
     */
    @FXThread
    private void changeTechnique(@Nullable final String newValue) {

        final Material currentMaterial = getCurrentMaterial();
        if (currentMaterial == null || newValue == null) {
            return;
        }

        final MaterialDef materialDef = getMaterialDef();
        final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(newValue);
        final TechniqueDef techniqueDef = techniqueDefs.get(0);

        final ShaderNodesContainer container = getShaderNodesContainer();
        container.show(techniqueDef);

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState != null) {
            //FIXME
        }

        EXECUTOR_MANAGER.addJMETask(() -> currentMaterial.selectTechnique(newValue, EDITOR.getRenderManager()));
    }

    /**
     * @return the project file.
     */
    private @NotNull ShaderNodesProject getProject() {
        return notNull(project);
    }

    /**
     * @param project the project file.
     */
    private void setProject(@NotNull final ShaderNodesProject project) {
        this.project = project;
    }

    /**
     * @return the current built material.
     */
    private @Nullable Material getCurrentMaterial() {
        return getProject().getMaterial();
    }

    /**
     * @param currentMaterial the current built material.
     */
    private void setCurrentMaterial(@Nullable final Material currentMaterial) {
        getProject().setMaterial(currentMaterial);
    }

    /**
     * Set the current edited material definition.
     *
     * @param materialDef the current edited material definition.
     */
    private void setMaterialDef(@NotNull final MaterialDef materialDef) {
        getProject().setMaterialDef(materialDef);

    }

    /**
     * @return the edited material definition.
     */
    private @NotNull MaterialDef getMaterialDef() {
        return notNull(getProject().getMaterialDef());
    }

    /**
     * Build material for shader nodes.
     */
    private void buildMaterial() {

        final Material currentMaterial = getCurrentMaterial();
        final MaterialDef materialDef = getMaterialDef();

        final Material newMaterial = new Material(clone(materialDef));

        if(currentMaterial != null) {
            MaterialUtils.migrateTo(newMaterial, currentMaterial);
        }

        final ComboBox<String> techniqueComboBox = getTechniqueComboBox();
        final ObservableList<String> items = techniqueComboBox.getItems();
        final SingleSelectionModel<String> selectionModel = techniqueComboBox.getSelectionModel();
        final String currentTechnique = selectionModel.getSelectedItem();

        final Collection<String> defsNames = materialDef.getTechniqueDefsNames();
        items.clear();
        items.addAll(defsNames);

        setCurrentMaterial(newMaterial);
        getEditor3DState().updateMaterial(newMaterial);

        if (items.contains(currentTechnique)) {
            selectionModel.select(currentTechnique);
        } else {
            selectionModel.select(TechniqueDef.DEFAULT_TECHNIQUE_NAME);
        }

        getSettingsTree().fill(new RootMaterialSettings(newMaterial));
    }

    private @NotNull MaterialDef clone(@NotNull final MaterialDef materialDef) {
        final Cloner cloner = new Cloner();
        final MaterialDef clone = cloner.clone(materialDef);
        return clone;
    }

    @Override
    @FromAnyThread
    public @NotNull EditorDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    @FXThread
    public void notifyClosed() {

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState == null) return;

        final ShaderNodesContainer shaderNodesContainer = getShaderNodesContainer();
        final Vector2f[] locations = shaderNodesContainer.getNodeElements()
                .stream().map(element -> new Vector2f((float) element.getLayoutX(), (float) element.getLayoutY()))
                .toArray(Vector2f[]::new);

        final double[] widths = shaderNodesContainer.getNodeElements()
                .stream().mapToDouble(Region::getPrefWidth)
                .toArray();

        editorState.updateNodeElementLocations(locations);
        editorState.updateNodeElementWidths(widths);


        super.notifyClosed();
    }

    @Override
    public @NotNull Vector2f[] getNodeElementLocations() {

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState == null) return new Vector2f[0];

        return editorState.getNodeElementLocations();
    }

    @Override
    public @NotNull double[] getNodeElementWidths() {

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState == null) return new double[0];

        return editorState.getNodeElementWidths();
    }

    @Override
    @FXThread
    public void notifyAddMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.refreshLines();
    }

    @Override
    @FXThread
    public void notifyRemoveMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.refreshLines();
    }

    @Override
    @FXThread
    public void notifyReplacedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping oldMapping,
                                      @NotNull final VariableMapping newMapping) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.refreshLines();
    }
}
