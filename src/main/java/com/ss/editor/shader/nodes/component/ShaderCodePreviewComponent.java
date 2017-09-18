package com.ss.editor.shader.nodes.component;

import static com.ss.editor.shader.nodes.ui.PluginCSSClasses.SHADER_CODE_PREVIEW_CONTAINER;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetManager;
import com.jme3.material.TechniqueDef;
import com.jme3.renderer.Caps;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.shader.Shader;
import com.jme3.shader.ShaderGenerator;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.annotation.JMEThread;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.ui.control.code.BaseCodeArea;
import com.ss.editor.ui.control.code.GLSLCodeArea;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.rlib.ui.util.FXUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

/**
 * The base implementation of preview a result shader.
 *
 * @author JavaSaBr
 */
public abstract class ShaderCodePreviewComponent extends CodePreviewComponent {

    /**
     * The box with language options.
     */
    @Nullable
    private ComboBox<String> languageBox;

    /**
     * The current shader.
     */
    @Nullable
    private volatile Shader shader;

    public ShaderCodePreviewComponent(@NotNull final AssetManager assetManager,
                                      @NotNull final RenderManager renderManager) {
        super(assetManager, renderManager);
        FXUtils.addClassTo(this, SHADER_CODE_PREVIEW_CONTAINER);
    }

    @Override
    @FXThread
    protected void createComponents() {

        final Label languageLabel  = new Label(PluginMessages.SHADER_PREVIEW_LANGUAGE + ":");
        languageLabel.minWidthProperty().bind(widthProperty().multiply(0.5));

        languageBox = new ComboBox<>();
        languageBox.prefWidthProperty().bind(widthProperty());
        languageBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> changeLanguage(newValue));

        final HBox languageContainer = new HBox(languageLabel, languageBox);

        codeArea = new GLSLCodeArea();
        codeArea.prefHeightProperty().bind(heightProperty());
        codeArea.loadContent("");
        codeArea.setEditable(false);

        FXUtils.addToPane(languageContainer, this);
        FXUtils.addToPane(codeArea, this);

        FXUtils.addClassTo(languageContainer, CSSClasses.DEF_HBOX);
    }

    /**
     * Get the shader type.
     *
     * @return the shader type.
     */
    @FXThread
    protected abstract @NotNull Shader.ShaderType getShaderType();

    /**
     * Handle changing the shader language.
     *
     * @param newValue the new shader language or null.
     */
    @FXThread
    private void changeLanguage(@Nullable final String newValue) {

        final BaseCodeArea codeArea = getCodeArea();

        if (newValue == null) {
            codeArea.loadContent("");
            return;
        }

        final Shader shader = getShader();
        final Optional<Shader.ShaderSource> result = shader.getSources().stream()
                .filter(shaderSource -> shaderSource.getType() == getShaderType())
                .filter(shaderSource -> shaderSource.getLanguage().equals(newValue))
                .findAny();

        if (!result.isPresent()) {
            codeArea.reloadContent("");
            return;
        }

        final Shader.ShaderSource source = result.get();
        codeArea.reloadContent(source.getSource());
    }

    /**
     * Load the technique definition.
     *
     * @param techniqueDef the technique definition.
     */
    @FromAnyThread
    public void load(@NotNull final TechniqueDef techniqueDef) {
        final ExecutorManager executorManager = ExecutorManager.getInstance();
        executorManager.addJMETask(() -> generateShader(techniqueDef));
    }

    /**
     * Generate shader from the technique definition.
     *
     * @param techniqueDef the technique definition.
     */
    @JMEThread
    private void generateShader(@NotNull final TechniqueDef techniqueDef) {

        final Renderer renderer = getRenderManager().getRenderer();
        final EnumSet<Caps> caps = renderer.getCaps();
        final ShaderGenerator shaderGenerator = getAssetManager().getShaderGenerator(caps);
        shaderGenerator.initialize(techniqueDef);

        this.shader = shaderGenerator.generateShader("");

        final ExecutorManager executorManager = ExecutorManager.getInstance();
        executorManager.addFXTask(this::loadShader);
    }

    /**
     * Load languages from the generated shader.
     */
    @FXThread
    private void loadShader() {

        final ComboBox<String> languageBox = getLanguageBox();
        final ObservableList<String> items = languageBox.getItems();
        items.clear();

        getShader().getSources().stream()
                .map(Shader.ShaderSource::getLanguage)
                .distinct()
                .forEach(items::add);

        if (!items.isEmpty()) {
            languageBox.getSelectionModel().select(items.get(0));
        }
    }

    /**
     * Get the box with language options.
     *
     * @return the box with language options.
     */
    @FXThread
    private @NotNull ComboBox<String> getLanguageBox() {
        return notNull(languageBox);
    }

    /**
     * Get the current shader.
     *
     * @return the current shader.
     */
    @FXThread
    protected @NotNull Shader getShader() {
        return notNull(shader);
    }
}
