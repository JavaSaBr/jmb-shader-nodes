package com.ss.editor.shader.nodes.editor;

import static com.ss.editor.extension.property.EditablePropertyType.ENUM;
import static com.ss.editor.extension.property.EditablePropertyType.STRING;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.StreamAssetInfo;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.export.binary.BinaryImporter;
import com.jme3.material.*;
import com.jme3.material.TechniqueDef.LightMode;
import com.jme3.material.logic.*;
import com.jme3.material.plugin.export.materialdef.J3mdExporter;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.math.Vector2f;
import com.jme3.shader.ShaderNode;
import com.jme3.shader.ShaderNodeVariable;
import com.jme3.shader.UniformBinding;
import com.jme3.shader.VariableMapping;
import com.ss.editor.FileExtensions;
import com.ss.editor.Messages;
import com.ss.editor.annotation.BackgroundThread;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.manager.ResourceManager;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.editor.material.BaseMaterialEditor3DState.ModelType;
import com.ss.editor.plugin.api.editor.material.BaseMaterialFileEditor;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ShaderNodesEditorPlugin;
import com.ss.editor.shader.nodes.component.FragmentShaderCodePreviewComponent;
import com.ss.editor.shader.nodes.component.MaterialDefCodePreviewComponent;
import com.ss.editor.shader.nodes.component.ShaderCodePreviewComponent;
import com.ss.editor.shader.nodes.component.VertexShaderCodePreviewComponent;
import com.ss.editor.shader.nodes.component.shader.ShaderNodesContainer;
import com.ss.editor.shader.nodes.component.shader.node.operation.add.AddTechniqueOperation;
import com.ss.editor.shader.nodes.editor.state.ShaderNodeState;
import com.ss.editor.shader.nodes.editor.state.ShaderNodeVariableState;
import com.ss.editor.shader.nodes.editor.state.ShaderNodesEditorState;
import com.ss.editor.shader.nodes.editor.state.TechniqueDefState;
import com.ss.editor.shader.nodes.model.PreviewMaterialSettings;
import com.ss.editor.shader.nodes.model.ShaderNodesProject;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.component.asset.tree.context.menu.action.DeleteFileAction;
import com.ss.editor.ui.component.asset.tree.context.menu.action.NewFileAction;
import com.ss.editor.ui.component.asset.tree.context.menu.action.RenameFileAction;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.editor.ui.component.tab.EditorToolComponent;
import com.ss.editor.ui.control.property.PropertyEditor;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.dialog.asset.virtual.StringVirtualAssetEditorDialog;
import com.ss.editor.ui.util.DynamicIconSupport;
import com.ss.editor.ui.util.UIUtils;
import com.ss.editor.util.EditorUtil;
import com.ss.editor.util.MaterialUtils;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.Utils;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The implementation of an editor to work with shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodesFileEditor extends
        BaseMaterialFileEditor<ShaderNodesEditor3DState, ShaderNodesEditorState, ShaderNodesChangeConsumer> implements
        ShaderNodesChangeConsumer {

    @NotNull
    private static final Predicate<Class<?>> ACTION_TESTER = type -> type == NewFileAction.class ||
            type == DeleteFileAction.class || type == RenameFileAction.class;

    /**
     * The description of this editor.
     */
    @NotNull
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    @NotNull
    private static final String PROP_TECHNIQUE_NAME = "name";

    @NotNull
    private static final String PROP_TECHNIQUE_LIGHT_MODE = "lightMode";

    static {
        DESCRIPTION.setConstructor(ShaderNodesFileEditor::new);
        DESCRIPTION.setEditorName(PluginMessages.SNS_EDITOR_NAME);
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
     * The fragment preview component.
     */
    @Nullable
    private ShaderCodePreviewComponent fragmentPreview;

    /**
     * The vertex preview component.
     */
    @Nullable
    private ShaderCodePreviewComponent vertexPreview;

    /**
     * The material definition preview component.
     */
    @Nullable
    private MaterialDefCodePreviewComponent matDefPreview;

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
    @FXThread
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
        getEditor3DState().updateMaterial(EDITOR.getDefaultMaterial());

        final ShaderNodesEditor3DState editor3DState = getEditor3DState();
        editor3DState.changeMode(ModelType.BOX);
    }

    @Override
    @FXThread
    protected void loadState() {
        super.loadState();

        EXECUTOR_MANAGER.addFXTask(this::buildMaterial);

        final ShaderNodesEditorState editorState = getEditorState();

        if (editorState != null) {
            editorState.cleanUp(getMaterialDef());
        }
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

    @Override
    @FXThread
    protected void createToolComponents(@NotNull final EditorToolComponent container, @NotNull final StackPane root) {
        super.createToolComponents(container, root);

        fragmentPreview = new FragmentShaderCodePreviewComponent(EDITOR.getAssetManager(), EDITOR.getRenderManager());
        fragmentPreview.prefHeightProperty().bind(root.heightProperty());

        vertexPreview = new VertexShaderCodePreviewComponent(EDITOR.getAssetManager(), EDITOR.getRenderManager());
        vertexPreview.prefHeightProperty().bind(root.heightProperty());

        matDefPreview = new MaterialDefCodePreviewComponent(EDITOR.getAssetManager(), EDITOR.getRenderManager());
        matDefPreview.prefHeightProperty().bind(root.heightProperty());

        container.addComponent(vertexPreview, PluginMessages.SNS_EDITOR_TOOL_VERTEX);
        container.addComponent(fragmentPreview, PluginMessages.SNS_EDITOR_TOOL_FRAGMENT);
        container.addComponent(matDefPreview, PluginMessages.SNS_EDITOR_TOOL_MD);
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

    /**
     * Get the fragment preview component.
     *
     * @return the fragment preview component.
     */
    @FXThread
    private @NotNull ShaderCodePreviewComponent getFragmentPreview() {
        return notNull(fragmentPreview);
    }

    /**
     * Get the vertex preview component.
     *
     * @return the vertex preview component.
     */
    @FXThread
    private @NotNull ShaderCodePreviewComponent getVertexPreview() {
        return notNull(vertexPreview);
    }

    /**
     * Get the material definition preview component.
     *
     * @return the material definition preview component.
     */
    @FXThread
    private @NotNull MaterialDefCodePreviewComponent getMatDefPreview() {
        return notNull(matDefPreview);
    }

    @Override
    @FXThread
    protected void createActions(@NotNull final HBox container) {
        super.createActions(container);

        final Button exportAction = new Button();
        exportAction.setTooltip(new Tooltip(PluginMessages.SNS_EDITOR_ACTION_EXPORT));
        exportAction.setOnAction(event -> export());
        exportAction.setGraphic(new ImageView(Icons.EXPORT_16));

        final Button importAction = new Button();
        importAction.setTooltip(new Tooltip(PluginMessages.SNS_EDITOR_ACTION_IMPORT));
        importAction.setOnAction(event -> importMatDef());
        importAction.setGraphic(new ImageView(Icons.IMPORT_16));

        FXUtils.addToPane(exportAction, importAction, container);

        FXUtils.addClassesTo(exportAction, importAction, CSSClasses.FLAT_BUTTON, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
        DynamicIconSupport.addSupport(exportAction, importAction);
    }

    @Override
    @FXThread
    protected void createToolbar(@NotNull final HBox container) {
        super.createToolbar(container);

        final Label techniqueLabel = new Label(PluginMessages.SNS_EDITOR_LABEL_TECHNIQUE + ":");

        techniqueComboBox = new ComboBox<>();
        techniqueComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> changeTechnique(newValue));

        final Button addTechnique = new Button();
        addTechnique.setTooltip(new Tooltip(PluginMessages.SNS_EDITOR_ACTION_ADD_TECHNIQUE));
        addTechnique.setOnAction(event -> addTechnique());
        addTechnique.setGraphic(new ImageView(Icons.ADD_16));

        DynamicIconSupport.addSupport(addTechnique);

        FXUtils.addToPane(techniqueLabel, techniqueComboBox, addTechnique, container);
        FXUtils.addClassTo(techniqueLabel, CSSClasses.FILE_EDITOR_TOOLBAR_LABEL);
        FXUtils.addClassTo(techniqueComboBox, CSSClasses.FILE_EDITOR_TOOLBAR_FIELD);
        FXUtils.addClassesTo(addTechnique, CSSClasses.FLAT_BUTTON, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
    }

    /**
     * Add new technique.
     */
    @FXThread
    private void addTechnique() {

        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(STRING, Messages.MODEL_PROPERTY_NAME, PROP_TECHNIQUE_NAME, "NewTechnique"));
        definitions.add(new PropertyDefinition(ENUM, PluginMessages.LIGHT_MODE, PROP_TECHNIQUE_LIGHT_MODE, LightMode.SinglePassAndImageBased));

        final GenericFactoryDialog dialog = new GenericFactoryDialog(definitions, this::addTechnique, this::validateTechnique);
        dialog.show();
    }

    @FXThread
    private boolean validateTechnique(@NotNull final VarTable vars) {
        final String name = vars.getString(PROP_TECHNIQUE_NAME);
        final MaterialDef materialDef = getMaterialDef();
        final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(name);
        return techniqueDefs == null && !name.isEmpty();
    }

    @FXThread
    private void addTechnique(@NotNull final VarTable vars) {

        final ShaderNodeVariable vertexGlobal = new ShaderNodeVariable("vec4", "Global", "position", null, "");
        vertexGlobal.setShaderOutput(true);

        final ShaderNodeVariable fragmentGlobal = new ShaderNodeVariable("vec4", "Global", "color", null, "");
        fragmentGlobal.setShaderOutput(true);
        fragmentGlobal.setCondition(null);
        fragmentGlobal.setMultiplicity(null);

        final ShaderGenerationInfo generationInfo = new ShaderGenerationInfo();
        generationInfo.setVertexGlobal(vertexGlobal);
        generationInfo.getFragmentGlobals().add(fragmentGlobal);

        final String name = vars.getString(PROP_TECHNIQUE_NAME);
        final LightMode lightMode = vars.getEnum(PROP_TECHNIQUE_LIGHT_MODE, LightMode.class);

        final TechniqueDef techniqueDef = new TechniqueDef(name, 0);
        techniqueDef.setShaderPrologue("");
        techniqueDef.setShaderNodes(new ArrayList<>());
        techniqueDef.setShaderGenerationInfo(generationInfo);
        techniqueDef.addWorldParam("");
        techniqueDef.setShaderFile(techniqueDef.hashCode() + "", techniqueDef.hashCode() + "",
                "GLSL100", "GLSL100");

        switch (lightMode) {
            case SinglePass: {
                techniqueDef.setLogic(new SinglePassLightingLogic(techniqueDef));
                break;
            }
            case SinglePassAndImageBased: {
                techniqueDef.setLogic(new SinglePassAndImageBasedLightingLogic(techniqueDef));
                break;
            }
            case StaticPass: {
                techniqueDef.setLogic(new StaticPassLightingLogic(techniqueDef));
                break;
            }
            case MultiPass: {
                techniqueDef.setLogic(new MultiPassLightingLogic(techniqueDef));
                break;
            }
            case Disable: {
                techniqueDef.setLogic(new DefaultTechniqueDefLogic(techniqueDef));
            }
        }

        execute(new AddTechniqueOperation(getMaterialDef(), techniqueDef));
    }

    /**
     * Export the result material definition as a file.
     */
    @FXThread
    private void export() {
        UIUtils.openSaveAsDialog(this::export, FileExtensions.JME_MATERIAL_DEFINITION, ACTION_TESTER);
    }

    /**
     * Import other material definition file to this project.
     */
    @FXThread
    private void importMatDef() {
        final ResourceManager resourceManager = ResourceManager.getInstance();
        final Array<String> resources = resourceManager.getAvailableResources(FileExtensions.JME_MATERIAL_DEFINITION);
        UIUtils.openResourceAssetDialog(this::importMatDef, this::validateMatDef, resources);
    }

    /**
     * Validate the selected material definition.
     *
     * @param assetPath the asset path.
     * @return the message or null if it's ok.
     */
    @FXThread
    private String validateMatDef(@NotNull final String assetPath) {

        final String message = StringVirtualAssetEditorDialog.DEFAULT_VALIDATOR.apply(assetPath);
        if (message != null) {
            return message;
        }

        final AssetManager assetManager = EDITOR.getAssetManager();
        final MaterialDef materialDef = (MaterialDef) assetManager.loadAsset(assetPath);
        final Collection<String> techniqueDefsNames = materialDef.getTechniqueDefsNames();

        for (final String techniqueDefsName : techniqueDefsNames) {
            final List<TechniqueDef> techniqueDefs = materialDef.getTechniqueDefs(techniqueDefsName);
            for (final TechniqueDef techniqueDef : techniqueDefs) {
                if (!techniqueDef.isUsingShaderNodes()) {
                    return PluginMessages.SNS_EDITOR_LABEL_INCORRECT_MD_TO_IMPORT;
                }
            }
        }

        return null;
    }

    /**
     * Export the result material definition as a file.
     *
     * @param path the file.
     */
    @FXThread
    private void export(@NotNull final Path path) {

        final J3mdExporter exporter = new J3mdExporter();
        final MaterialDef materialDef = getMaterialDef();
        final MaterialDef clone = clone(materialDef);
        clone.setAssetName(getFileName());

        try (final OutputStream out = Files.newOutputStream(path)) {
            exporter.save(clone, out);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
        }
    }

    /**
     * Import other material definition file to this project.
     *
     * @param resource the resource.
     */
    @FXThread
    private void importMatDef(@NotNull final String resource) {

        final AssetManager assetManager = EDITOR.getAssetManager();
        final MaterialDef matDef = (MaterialDef) assetManager.loadAsset(resource);

        setMaterialDef(matDef);
        buildMaterial();
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
        container.notifyChangedMaterial();

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState != null) {
            //FIXME
        }

        getFragmentPreview().load(techniqueDef);
        getVertexPreview().load(techniqueDef);
        getEditor3DState().selectTechnique(currentMaterial, newValue);
    }

    /**
     * @return the project file.
     */
    @FXThread
    private @NotNull ShaderNodesProject getProject() {
        return notNull(project);
    }

    /**
     * @param project the project file.
     */
    @FXThread
    private void setProject(@NotNull final ShaderNodesProject project) {
        this.project = project;
    }

    /**
     * @return the current built material.
     */
    @FXThread
    private @Nullable Material getCurrentMaterial() {
        return currentMaterial;
    }

    /**
     * @param currentMaterial the current built material.
     */
    @FXThread
    private void setCurrentMaterial(@Nullable final Material currentMaterial) {
        this.currentMaterial = currentMaterial;
    }

    /**
     * Set the current edited material definition.
     *
     * @param materialDef the current edited material definition.
     */
    @FXThread
    private void setMaterialDef(@NotNull final MaterialDef materialDef) {
        this.materialDef = materialDef;
    }

    @Override
    @FromAnyThread
    public @NotNull MaterialDef getMaterialDef() {
        return notNull(materialDef);
    }

    @Override
    @FromAnyThread
    public @Nullable Material getPreviewMaterial() {
        return currentMaterial;
    }

    /**
     * Build material for shader nodes.
     */
    @FXThread
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
        getShaderNodesContainer().notifyChangedMaterial();
        getEditor3DState().updateMaterial(newMaterial);
        getMatDefPreview().load(newMaterial.getMaterialDef());

        if (items.contains(currentTechnique)) {
            selectionModel.select(currentTechnique);
        } else {
            selectionModel.select(TechniqueDef.DEFAULT_TECHNIQUE_NAME);
        }

        getSettingsTree().fill(new PreviewMaterialSettings(newMaterial));
    }

    /**
     * Clone the material definition.
     *
     * @param materialDef the material definition.
     * @return the cloned.
     */
    @FromAnyThread
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
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.addNodeElement(variable, location);
    }

    @FXThread
    @Override
    public void notifyRemovedAttribute(@NotNull final ShaderNodeVariable variable) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.removeNodeElement(variable);
    }

    @Override
    @FXThread
    public void notifyAddedWorldParameter(@NotNull final UniformBinding binding, @NotNull final Vector2f location) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.addWorldParam(binding, location);
    }

    @Override
    @FXThread
    public void notifyRemovedWorldParameter(@NotNull final UniformBinding binding) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.removeWorldParam(binding);
    }

    @Override
    @FXThread
    public void notifyAddedShaderNode(@NotNull final ShaderNode shaderNode, @NotNull final Vector2f location) {
        buildMaterial();
        final ShaderNodesContainer container = getShaderNodesContainer();
        container.addShaderNode(shaderNode, location);
    }

    @Override
    @FXThread
    public void notifyRemovedRemovedShaderNode(@NotNull final ShaderNode shaderNode) {
        buildMaterial();
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

    @Override
    @FXThread
    public @Nullable Vector2f getGlobalNodeLocation(final boolean input) {
        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return null;
        return input ? state.getInputNodeLocation() : state.getOutputNodeLocation();
    }

    @Override
    @FXThread
    public double getGlobalNodeWidth(final boolean input) {
        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return 0D;
        return input ? state.getInputNodeWidth() : state.getOutputNodeWidth();
    }

    @Override
    @FXThread
    public void notifyAddedTechnique(@NotNull final TechniqueDef techniqueDef) {
        buildMaterial();
    }

    @Override
    @FXThread
    public void notifyRemovedTechnique(@NotNull final TechniqueDef techniqueDef) {
        buildMaterial();
    }

    @Override
    @FXThread
    public void notifyChangeGlobalNodeState(final boolean input, @NotNull final Vector2f location, final double width) {

        final TechniqueDefState state = getTechniqueDefState();
        if (state == null) return;

        if (input) {
            state.setInputNodeLocation(location);
            state.setInputNodeWidth((int) width);
        } else {
            state.setOutputNodeLocation(location);
            state.setOutputNodeWidth((int) width);
        }
    }

    /**
     * Get the current technique definition state.
     *
     * @return the current technique definition state.
     */
    @FXThread
    private @Nullable TechniqueDefState getTechniqueDefState() {

        final ShaderNodesEditorState editorState = getEditorState();
        final ComboBox<String> techniqueComboBox = getTechniqueComboBox();
        final String currentTech = techniqueComboBox.getSelectionModel().getSelectedItem();

        if (currentTech == null || editorState == null) {
            return null;
        }

        return editorState.getState(currentTech);
    }

    @Override
    @FXThread
    public void notifyFXChangeProperty(@NotNull final Object object, @NotNull final String propertyName) {
        super.notifyFXChangeProperty(object, propertyName);

        if (object instanceof MatParam) {
            final PropertyEditor<ShaderNodesChangeConsumer> propertyEditor = getPropertyEditor();
            propertyEditor.refresh();
        }

        getShaderNodesContainer().notifyChangedMaterial();
    }
}
