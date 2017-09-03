package com.ss.editor.shader.nodes.editor;

import static com.jme3.renderer.queue.RenderQueue.Bucket.Inherit;
import static com.jme3.renderer.queue.RenderQueue.Bucket.values;
import static com.ss.editor.util.EditorUtil.getAssetFile;
import static com.ss.editor.util.EditorUtil.toAssetPath;
import static com.ss.rlib.util.ObjectUtils.notNull;
import static javafx.collections.FXCollections.observableArrayList;
import com.jme3.asset.AssetManager;
import com.jme3.material.*;
import com.jme3.renderer.queue.RenderQueue;
import com.ss.editor.FileExtensions;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.model.node.material.RootMaterialSettings;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.editor.Advanced3DFileEditor;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.editor.state.editor.impl.material.MaterialEditor3DState;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.component.editor.EditorDescription;
import com.ss.editor.ui.component.editor.state.EditorState;
import com.ss.editor.ui.control.property.PropertyEditor;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.util.DynamicIconSupport;
import com.ss.editor.util.MaterialUtils;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.Utils;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * The implementation of an editor to work with shader nodes.
 *
 * @author JavaSaBr
 */
public class ShaderNodesFileEditor extends Advanced3DFileEditor<ShaderNodesEditor3DState, ShaderNodesEditorState> {

    /**
     * The default flag of enabling light.
     */
    public static final boolean DEFAULT_LIGHT_ENABLED = true;

    /**
     * The constant DESCRIPTION.
     */
    @NotNull
    public static final EditorDescription DESCRIPTION = new EditorDescription();

    static {
        DESCRIPTION.setConstructor(ShaderNodesFileEditor::new);
        DESCRIPTION.setEditorName("Shader Nodes Editor");
        DESCRIPTION.setEditorId(ShaderNodesFileEditor.class.getSimpleName());
        DESCRIPTION.addExtension(FileExtensions.JME_MATERIAL_DEFINITION);
    }

    @NotNull
    private static final ObservableList<RenderQueue.Bucket> BUCKETS = observableArrayList(values());

    /**
     * The area to show a material.
     */
    @Nullable
    private BorderPane materialArea;

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
     * The settings tree.
     */
    @Nullable
    private NodeTree<ChangeConsumer> settingsTree;

    /**
     * The property editor.
     */
    @Nullable
    private PropertyEditor<ChangeConsumer> propertyEditor;

    /**
     * The edited material definition.
     */
    @Nullable
    private MaterialDef materialDef;

    /**
     * The current built material.
     */
    @Nullable
    private Material currentMaterial;

    /**
     * The button to use a cube.
     */
    @Nullable
    private ToggleButton cubeButton;

    /**
     * The button to use a sphere.
     */
    @Nullable
    private ToggleButton sphereButton;

    /**
     * The button to use a plane.
     */
    @Nullable
    private ToggleButton planeButton;

    /**
     * The button to use a light.
     */
    @Nullable
    private ToggleButton lightButton;

    /**
     * The list of RenderQueue.Bucket.
     */
    @Nullable
    private ComboBox<RenderQueue.Bucket> bucketComboBox;

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
    protected void doOpenFile(@NotNull final Path file) {
        super.doOpenFile(file);

        final Path assetFile = notNull(getAssetFile(file));
        final AssetManager assetManager = EDITOR.getAssetManager();

        final MaterialDef materialDef = (MaterialDef) assetManager.loadAsset(toAssetPath(assetFile));

        final ShaderNodesEditor3DState editor3DState = getEditor3DState();
        editor3DState.changeMode(ShaderNodesEditor3DState.ModelType.BOX);

        setMaterialDef(clone(materialDef));
        buildMaterial();
    }

    @Override
    @FXThread
    protected void createContent(@NotNull final StackPane root) {

        settingsTree = new NodeTree<>(this::selectFromTree, this);
        propertyEditor = new PropertyEditor<>(this);
        propertyEditor.prefHeightProperty().bind(root.heightProperty());
        shaderNodesContainer = new ShaderNodesContainer();

        shaderNodesArea = new BorderPane(shaderNodesContainer);
        materialArea = new BorderPane();

        final SplitPane materialSettingsSplitPanel = new SplitPane(settingsTree, propertyEditor);

        final SplitPane shaderNodesMaterialAreaSplitPanel = new SplitPane(shaderNodesArea, materialArea);

        final SplitPane shaderAndMaterialsAreaMaterialSettingsSplitPanel = new SplitPane(shaderNodesMaterialAreaSplitPanel, materialSettingsSplitPanel);
        shaderAndMaterialsAreaMaterialSettingsSplitPanel.prefWidthProperty().bind(root.widthProperty());

        root.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            materialSettingsSplitPanel.setDividerPosition(0, 0.2);
            shaderNodesMaterialAreaSplitPanel.setDividerPosition(0, 0.7);
        });
        root.widthProperty().addListener((observable, oldValue, newValue) -> {
            shaderAndMaterialsAreaMaterialSettingsSplitPanel.setDividerPosition(0, 0.8);
        });

        FXUtils.addClassTo(materialSettingsSplitPanel, CSSClasses.FILE_EDITOR_TOOL_SPLIT_PANE);
        FXUtils.addClassTo(shaderNodesMaterialAreaSplitPanel, CSSClasses.FILE_EDITOR_TOOL_SPLIT_PANE);
        FXUtils.addClassTo(shaderAndMaterialsAreaMaterialSettingsSplitPanel, CSSClasses.FILE_EDITOR_MAIN_SPLIT_PANE);
        FXUtils.addClassTo(settingsTree.getTreeView(), CSSClasses.TRANSPARENT_TREE_VIEW);

        FXUtils.addToPane(shaderAndMaterialsAreaMaterialSettingsSplitPanel, root);
    }

    /**
     * Select object from tree.
     *
     * @param object the selected object.
     */
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
    protected void loadState() {
        super.loadState();

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState == null) {
            return;
        }

        switch (MaterialEditor3DState.ModelType.valueOf(editorState.getModelType())) {
            case BOX:
                getCubeButton().setSelected(true);
                break;
            case SPHERE:
                getSphereButton().setSelected(true);
                break;
            case QUAD:
                getPlaneButton().setSelected(true);
                break;
        }

        getBucketComboBox().getSelectionModel().select(editorState.getBucketType());
        getLightButton().setSelected(editorState.isLightEnable());
    }

    @Override
    @FXThread
    public @Nullable BorderPane get3DArea() {
        return materialArea;
    }

    @Override
    @FXThread
    protected boolean needToolbar() {
        return true;
    }

    /**
     * @return the button to use a cube.
     */
    @FromAnyThread
    private @NotNull ToggleButton getCubeButton() {
        return notNull(cubeButton);
    }

    /**
     * @return the button to use a plane.
     */
    @FromAnyThread
    private @NotNull ToggleButton getPlaneButton() {
        return notNull(planeButton);
    }

    /**
     * @return the button to use a sphere.
     */
    @FromAnyThread
    private @NotNull ToggleButton getSphereButton() {
        return notNull(sphereButton);
    }

    /**
     * @return the button to use a light.
     */
    @FromAnyThread
    private @NotNull ToggleButton getLightButton() {
        return notNull(lightButton);
    }

    /**
     * @return the list of RenderQueue.Bucket.
     */
    @FromAnyThread
    private @NotNull ComboBox<RenderQueue.Bucket> getBucketComboBox() {
        return notNull(bucketComboBox);
    }

    /**
     * @return The list of available techniques.
     */
    @FromAnyThread
    private @NotNull ComboBox<String> getTechniqueComboBox() {
        return notNull(techniqueComboBox);
    }

    /**
     * @return the property editor.
     */
    @FromAnyThread
    private @NotNull PropertyEditor<ChangeConsumer> getPropertyEditor() {
        return notNull(propertyEditor);
    }

    /**
     * @return the settings tree.
     */
    @FromAnyThread
    private @NotNull NodeTree<ChangeConsumer> getSettingsTree() {
        return notNull(settingsTree);
    }

    @FXThread
    @Override
    protected void createToolbar(@NotNull final HBox container) {
        super.createToolbar(container);

        cubeButton = new ToggleButton();
        cubeButton.setTooltip(new Tooltip(Messages.MATERIAL_FILE_EDITOR_ACTION_CUBE + " (C)"));
        cubeButton.setGraphic(new ImageView(Icons.CUBE_16));
        cubeButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeModelType(MaterialEditor3DState.ModelType.BOX, newValue));

        sphereButton = new ToggleButton();
        sphereButton.setTooltip(new Tooltip(Messages.MATERIAL_FILE_EDITOR_ACTION_SPHERE + " (S)"));
        sphereButton.setGraphic(new ImageView(Icons.SPHERE_16));
        sphereButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeModelType(MaterialEditor3DState.ModelType.SPHERE, newValue));

        planeButton = new ToggleButton();
        planeButton.setTooltip(new Tooltip(Messages.MATERIAL_FILE_EDITOR_ACTION_PLANE + " (P)"));
        planeButton.setGraphic(new ImageView(Icons.PLANE_16));
        planeButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                changeModelType(MaterialEditor3DState.ModelType.QUAD, newValue));

        lightButton = new ToggleButton();
        lightButton.setTooltip(new Tooltip(Messages.MATERIAL_FILE_EDITOR_ACTION_LIGHT + " (L)"));
        lightButton.setGraphic(new ImageView(Icons.LIGHT_16));
        lightButton.setSelected(DEFAULT_LIGHT_ENABLED);
        lightButton.selectedProperty().addListener((observable, oldValue, newValue) -> changeLight(newValue));

        final Label bucketLabel = new Label(Messages.MATERIAL_FILE_EDITOR_BUCKET_TYPE_LABEL + ":");

        bucketComboBox = new ComboBox<>(BUCKETS);
        bucketComboBox.getSelectionModel().select(Inherit);
        bucketComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> changeBucketType(newValue));

        final Label techniqueLabel = new Label("Technique:");

        techniqueComboBox = new ComboBox<>();
        techniqueComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> changeTechnique(newValue));

        FXUtils.addToPane(createSaveAction(), container);
        FXUtils.addToPane(cubeButton, container);
        FXUtils.addToPane(sphereButton, container);
        FXUtils.addToPane(planeButton, container);
        FXUtils.addToPane(lightButton, container);
        FXUtils.addToPane(bucketLabel, container);
        FXUtils.addToPane(bucketComboBox, container);
        FXUtils.addToPane(techniqueLabel, container);
        FXUtils.addToPane(techniqueComboBox, container);

        DynamicIconSupport.addSupport(cubeButton, sphereButton, planeButton, lightButton);

        FXUtils.addClassTo(bucketLabel, techniqueLabel, CSSClasses.FILE_EDITOR_TOOLBAR_LABEL);
        FXUtils.addClassTo(bucketComboBox, techniqueComboBox, CSSClasses.FILE_EDITOR_TOOLBAR_FIELD);
        FXUtils.addClassTo(cubeButton, sphereButton, planeButton, lightButton, CSSClasses.FILE_EDITOR_TOOLBAR_BUTTON);
    }

    /**
     * Handle the changed model type.
     */
    @FXThread
    private void changeModelType(@NotNull final MaterialEditor3DState.ModelType modelType, @NotNull final Boolean newValue) {
        if (newValue == Boolean.FALSE) return;

        final ShaderNodesEditor3DState editor3DState = getEditor3DState();

        final ToggleButton cubeButton = getCubeButton();
        final ToggleButton sphereButton = getSphereButton();
        final ToggleButton planeButton = getPlaneButton();

        if (modelType == MaterialEditor3DState.ModelType.BOX) {
            cubeButton.setMouseTransparent(true);
            sphereButton.setMouseTransparent(false);
            planeButton.setMouseTransparent(false);
            cubeButton.setSelected(true);
            sphereButton.setSelected(false);
            planeButton.setSelected(false);
            editor3DState.changeMode(modelType);
        } else if (modelType == MaterialEditor3DState.ModelType.SPHERE) {
            cubeButton.setMouseTransparent(false);
            sphereButton.setMouseTransparent(true);
            planeButton.setMouseTransparent(false);
            cubeButton.setSelected(false);
            sphereButton.setSelected(true);
            planeButton.setSelected(false);
            editor3DState.changeMode(modelType);
        } else if (modelType == MaterialEditor3DState.ModelType.QUAD) {
            cubeButton.setMouseTransparent(false);
            sphereButton.setMouseTransparent(false);
            planeButton.setMouseTransparent(true);
            sphereButton.setSelected(false);
            cubeButton.setSelected(false);
            planeButton.setSelected(true);
            editor3DState.changeMode(modelType);
        }

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState != null) editorState.setModelType(modelType);
    }

    /**
     * Handle changing the light enabling.
     */
    @FXThread
    private void changeLight(@NotNull final Boolean newValue) {

        final ShaderNodesEditor3DState editor3DState = getEditor3DState();
        editor3DState.updateLightEnabled(newValue);

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState != null) editorState.setLightEnable(newValue);
    }

    /**
     * Handle changing the bucket type.
     */
    @FXThread
    private void changeBucketType(@NotNull final RenderQueue.Bucket newValue) {

        final ShaderNodesEditor3DState editor3DState = getEditor3DState();
        editor3DState.changeBucketType(newValue);

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState != null) editorState.setBucketType(newValue);
    }

    /**
     * Handle changing the technique.
     */
    @FXThread
    private void changeTechnique(@NotNull final String newValue) {

        final Material currentMaterial = getCurrentMaterial();
        if (currentMaterial == null) {
            return;
        }

        EXECUTOR_MANAGER.addJMETask(() -> currentMaterial.selectTechnique(newValue, EDITOR.getRenderManager()));

        final ShaderNodesEditorState editorState = getEditorState();
        if (editorState != null) {
            //FIXME
        }
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

    /**
     * @return the edited material definition.
     */
    private @NotNull MaterialDef getMaterialDef() {
        return materialDef;
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
    public void notifyFXChangeProperty(@NotNull final Object object, @NotNull final String propertyName) {
        if (object instanceof Material) {
            getPropertyEditor().refresh();
        } else {
            getPropertyEditor().syncFor(object);
        }
    }
}
