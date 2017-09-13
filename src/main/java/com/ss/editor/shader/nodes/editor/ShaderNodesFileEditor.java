package com.ss.editor.shader.nodes.editor;

import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.StreamAssetInfo;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.material.*;
import com.jme3.material.plugin.export.materialdef.J3mdExporter;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.VariableMapping;
import com.ss.editor.annotation.BackgroundThread;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.node.material.RootMaterialSettings;
import com.ss.editor.plugin.api.editor.material.BaseMaterialFileEditor;
import com.ss.editor.shader.nodes.ShaderNodesEditorPlugin;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.editor.state.ShaderNodeState;
import com.ss.editor.shader.nodes.editor.state.ShaderNodeVariableState;
import com.ss.editor.shader.nodes.editor.state.ShaderNodesEditorState;
import com.ss.editor.shader.nodes.editor.state.TechniqueDefState;
import com.ss.editor.shader.nodes.model.ShaderNodesProject;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.util.MaterialUtils;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.Utils;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
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
     * The current material definition.
     */
    @Nullable
    private MaterialDef materialDef;

    /**
     * The current material.
     */
    @Nullable
    private Material currentMaterial;

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

        final AssetManager assetManager = EDITOR.getAssetManager();
        final BinaryImporter importer = BinaryImporter.getInstance();
        importer.setAssetManager(assetManager);

        try (final InputStream in = Files.newInputStream(file)) {
            setProject((ShaderNodesProject) importer.load(in));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final ShaderNodesProject project = getProject();
        final String materialDefContent = project.getMaterialDefContent();

        final ByteArrayInputStream materialDefStream =
                new ByteArrayInputStream(materialDefContent.getBytes("UTF-8"));

        final AssetKey<MaterialDef> tempKey = new AssetKey<>("tempMatDef");
        final StreamAssetInfo assetInfo = new StreamAssetInfo(assetManager, tempKey, materialDefStream);

        final J3MLoader loader = new J3MLoader();
        final MaterialDef materialDef = (MaterialDef) loader.load(assetInfo);

        setMaterialDef(materialDef);

        final ShaderNodesEditor3DState editor3DState = getEditor3DState();
        editor3DState.changeMode(ShaderNodesEditor3DState.ModelType.BOX);

        EXECUTOR_MANAGER.addFXTask(this::buildMaterial);
    }

    @Override
    @BackgroundThread
    protected void doSave(@NotNull final Path toStore) throws IOException {
        super.doSave(toStore);

        final BinaryExporter exporter = BinaryExporter.getInstance();

        final MaterialDef materialDef = getMaterialDef();
        final Material currentMaterial = getCurrentMaterial();
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();

        final J3mdExporter materialExporter = new J3mdExporter();
        materialExporter.save(materialDef, bout);

        final String materialDefContent = new String(bout.toByteArray(), "UTF-8");

        final ShaderNodesProject project = getProject();
        project.setMaterialDefContent(materialDefContent);

        if (currentMaterial != null) {
            project.setMatParams(currentMaterial.getParams());
        }

        try (final OutputStream out = Files.newOutputStream(toStore)) {
            exporter.save(project, out);
        }
    }

    @Override
    @FXThread
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

    @Override
    @FXThread
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
        return currentMaterial;
    }

    /**
     * @param currentMaterial the current built material.
     */
    private void setCurrentMaterial(@Nullable final Material currentMaterial) {
        this.currentMaterial = currentMaterial;
    }

    /**
     * Set the current edited material definition.
     *
     * @param materialDef the current edited material definition.
     */
    private void setMaterialDef(@NotNull final MaterialDef materialDef) {
        this.materialDef = materialDef;
    }

    @Override
    public @NotNull MaterialDef getMaterialDef() {
        return notNull(materialDef);
    }

    /**
     * Build material for shader nodes.
     */
    private void buildMaterial() {

        final Material currentMaterial = getCurrentMaterial();
        final MaterialDef materialDef = getMaterialDef();

        final Material newMaterial = new Material(clone(materialDef));

        if (currentMaterial != null) {
            MaterialUtils.migrateTo(newMaterial, currentMaterial);
        } else {

            final ShaderNodesProject project = getProject();
            final List<MatParam> matParams = project.getMatParams();

            for (final MatParam matParam : matParams) {
                if (matParam instanceof MatParamTexture) {
                    final MatParamTexture paramTexture = (MatParamTexture) matParam;
                    newMaterial.setTextureParam(matParam.getName(), matParam.getVarType(), paramTexture.getTextureValue());
                } else {
                    newMaterial.setParam(matParam.getName(), matParam.getVarType(), matParam.getValue());
                }
            }
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

        final Collection<MatParam> materialParams = materialDef.getMaterialParams();
        final Collection<String> techniqueDefsNames = materialDef.getTechniqueDefsNames();

        final MaterialDef newMaterialDef = new MaterialDef(EDITOR.getAssetManager(), materialDef.getAssetName());

        materialParams.stream()
                .filter(MatParamTexture.class::isInstance)
                .map(MatParamTexture.class::cast)
                .forEach(param -> newMaterialDef.addMaterialParamTexture(param.getVarType(), param.getName(), param.getColorSpace()));
        materialParams.stream()
                .filter(param -> !(param instanceof MatParamTexture))
                .forEach(param -> newMaterialDef.addMaterialParam(param.getVarType(), param.getName(), param.getValue()));

        for (final String defsName : techniqueDefsNames) {

            final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(defsName);

            for (final TechniqueDef techniqueDef : techniqueDefs) {
                newMaterialDef.addTechniqueDef(notNull(Utils.get(techniqueDef, TechniqueDef::clone)));
            }
        }

        return newMaterialDef;
    }

    @Override
    @FromAnyThread
    public @NotNull EditorDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    @FXThread
    public void notifyAddedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.refreshLines();
    }

    @Override
    @FXThread
    public void notifyRemovedMapping(@NotNull final ShaderNode shaderNode, @NotNull final VariableMapping mapping) {
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

    @FXThread
    @Override
    public void notifyAddedMatParameter(@NotNull final MatParam matParam, @NotNull final Vector2f location) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.addMatParam(matParam, location);
    }

    @FXThread
    @Override
    public void notifyRemovedMatParameter(@NotNull final MatParam matParam) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.removeMatParam(matParam);
    }

    @FXThread
    @Override
    public void notifyAddedAttribute(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location) {
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.addNodeElement(variable, location);
    }

    @FXThread
    @Override
    public void notifyRemovedAttribute(@NotNull final ShaderNodeVariable variable) {
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.removeNodeElement(variable);
    }

    @Override
    @FXThread
    public void notifyAddedWorldParameter(@NotNull final UniformBinding binding, @NotNull final Vector2f location) {
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.addWorldParam(binding, location);
    }

    @Override
    @FXThread
    public void notifyRemovedWorldParameter(@NotNull final UniformBinding binding) {
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.removeWorldParam(binding);
    }

    @Override
    @FXThread
    public void notifyAddedShaderNode(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location) {
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.addShaderNode(shaderNode, location);
    }

    @Override
    @FXThread
    public void notifyRemovedRemovedShaderNode(@NotNull final ShaderNode shaderNode) {
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.removeShaderNode(shaderNode);
    }

    @Override
    @FXThread
    public void notifyChangeState(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location,
                                  final double width) {

        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return;
        state.notifyChange(shaderNode, location, width);
    }

    @Override
    @FXThread
    public void notifyChangeState(@NotNull final ShaderNodeVariable variable, @NotNull final Vector2f location,
                                  final double width) {

        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return;
        state.notifyChange(variable, location, width);
    }

    @Override
    @FXThread
    public @Nullable Vector2f getLocation(@NotNull final ShaderNode shaderNode) {
        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return null;
        final ShaderNodeState nodeState = state.getState(shaderNode);
        return nodeState == null ? null : nodeState.getLocation();
    }

    @Override
    @FXThread
    public @Nullable Vector2f getLocation(@NotNull final ShaderNodeVariable variable) {
        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return null;
        final ShaderNodeVariableState variableState = state.getState(variable);
        return variableState == null ? null : variableState.getLocation();
    }

    @Override
    @FXThread
    public double getWidth(@NotNull final ShaderNode shaderNode) {
        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return 0D;
        final ShaderNodeState nodeState = state.getState(shaderNode);
        return nodeState == null ? 0D : nodeState.getWidth();
    }

    @Override
    @FXThread
    public double getWidth(@NotNull final ShaderNodeVariable variable) {
        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return 0D;
        final ShaderNodeVariableState variableState = state.getState(variable);
        return variableState == null ? 0D : variableState.getWidth();
    }

    /**
     * Get the current technique definition state.
     *
     * @return the current technique definition state.
     */
    private @Nullable TechniqueDefState getTechniqueDefState() {

        final ShaderNodesEditorState editorState = getEditorState();
        final ComboBox<String> techniqueComboBox = getTechniqueComboBox();
        final String currentTech = techniqueComboBox.getSelectionModel().getSelectedItem();

        if (currentTech == null || editorState == null) {
            return null;
        }

        return editorState.getState(currentTech);
    }
}
