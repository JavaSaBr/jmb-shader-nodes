package com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter;

import static com.ss.editor.shader.nodes.ui.PluginCssClasses.SHADER_NODE_INPUT_PARAMETER;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.shader.nodes.ui.PluginCssClasses;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodeElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.InputSocketElement;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.parameter.socket.SocketElement;
import com.ss.editor.ui.css.CssClasses;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;

/**
 * The implementation of input shader nodes parameter.
 *
 * @author JavaSaBr
 */
public class InputShaderNodeParameter extends ShaderNodeParameter {


    /**
     * The check box to use expression.
     */
    @NotNull
    private CheckBox useExpression;

    /**
     * The label to use an expression.
     */
    @NotNull
    private Label useExpressionLabel;

    /**
     * The field with expression.
     */
    @NotNull
    private TextField expressionField;

    public InputShaderNodeParameter(@NotNull final ShaderNodeElement<?> nodeElement,
                                    @NotNull final ShaderNodeVariable variable) {
        super(nodeElement, variable);
        FXUtils.addClassTo(this, SHADER_NODE_INPUT_PARAMETER);
    }

    /**
     * Get the checkbox of using expression.
     *
     * @return the checkbox of using expression.
     */
    @FxThread
    protected @NotNull CheckBox getUseExpression() {
        return useExpression;
    }

    /**
     * Get the use expression label.
     *
     * @return the use expression label.
     */
    @FxThread
    protected @NotNull Label getUseExpressionLabel() {
        return useExpressionLabel;
    }

    /**
     * Get the expression field.
     *
     * @return the expression field.
     */
    @FxThread
    protected @NotNull TextField getExpressionField() {
        return expressionField;
    }

    @Override
    @FxThread
    protected @NotNull SocketElement createSocket() {
        return new InputSocketElement(this);
    }

    @Override
    @FxThread
    protected void createContent() {
        super.createContent();

        this.useExpressionLabel = new Label("expr:");
        this.useExpression = new CheckBox();
        this.expressionField = new TextField();
        this.expressionField.prefWidthProperty().bind(widthProperty());

        var parameterContainer = new HBox(getSocket(), getNameLabel(), getTypeLabel());
        parameterContainer.prefWidthProperty().bind(widthProperty());

        var expressionContainer = new HBox(expressionField);
        expressionContainer.managedProperty().bind(useExpression.selectedProperty());
        expressionContainer.visibleProperty().bind(useExpression.selectedProperty());
        expressionContainer.prefWidthProperty().bind(widthProperty());

        add(parameterContainer, 0, 0);
        add(getUseExpressionLabel(), 1, 0);
        add(getUseExpression(), 2, 0);
        add(expressionContainer, 0, 1, 3, 1);

        FXUtils.addClassTo(parameterContainer, PluginCssClasses.SHADER_NODE_INPUT_PARAMETER_CONTAINER);
        FXUtils.addClassTo(expressionContainer, CssClasses.DEF_HBOX);
    }
}
