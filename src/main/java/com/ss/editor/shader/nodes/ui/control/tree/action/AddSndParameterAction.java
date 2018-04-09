package com.ss.editor.shader.nodes.ui.control.tree.action;

import static com.ss.editor.extension.property.EditablePropertyType.ENUM;
import static com.ss.editor.extension.property.EditablePropertyType.STRING;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndParameters;
import com.ss.editor.shader.nodes.ui.control.tree.operation.AddSndParameterOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.util.GlslType;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The action to add new parameter.
 *
 * @author JavaSaBr
 */
public abstract class AddSndParameterAction extends AbstractNodeAction<ChangeConsumer> {

    @NotNull
    private static final String PROP_NAME = "name";

    @NotNull
    private static final String PROP_TYPE = "type";

    public AddSndParameterAction(@NotNull NodeTree<?> nodeTree, @NotNull TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return PluginMessages.ACTION_ADD_SND_PARAMETER;
    }

    @Override
    @FxThread
    protected @Nullable Image getIcon() {
        return Icons.ADD_16;
    }

    /**
     * Get the list of current parameters.
     *
     * @param definition the definition.
     * @return the list of current parameters.
     */
    @FxThread
    protected abstract @NotNull List<ShaderNodeVariable> getCurrentParameters(
            @NotNull ShaderNodeDefinition definition
    );

    /**
     * Get the list of opposite parameters.
     *
     * @param definition the definition.
     * @return the list of opposite parameters.
     */
    @FxThread
    protected abstract @NotNull List<ShaderNodeVariable> getOppositeParameters(
            @NotNull ShaderNodeDefinition definition
    );

    @Override
    @FxThread
    protected void process() {
        super.process();

        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(STRING, Messages.MODEL_PROPERTY_NAME, PROP_NAME, "newVar"));
        definitions.add(new PropertyDefinition(ENUM, Messages.MODEL_PROPERTY_TYPE, PROP_TYPE, GlslType.FLOAT));

        var dialog = new GenericFactoryDialog(definitions, this::addParameter, this::validate);
        dialog.setTitle(getName());
        dialog.show();
    }

    /**
     * Validate the variables.
     *
     * @param vars the vars of the new parameter.
     */
    @FxThread
    private boolean validate(@NotNull VarTable vars) {

        var node = getNode();
        var parameters = (SndParameters) node.getElement();
        var definition = parameters.getDefinition();

        var name = vars.getString(PROP_NAME);
        var exists = getCurrentParameters(definition).stream()
                .anyMatch(variable -> variable.getName().equals(name));

        if (exists) {
            return false;
        }

        var glslType = vars.getEnum(PROP_TYPE, GlslType.class);
        var rawType = glslType.getRawType();

        var oppositeParameters = getOppositeParameters(definition);
        var oppositeParameter = oppositeParameters.stream()
                .filter(variable -> variable.getName().equals(name))
                .findAny();

        if (!oppositeParameter.isPresent()) {
            return true;
        }

        var variable = oppositeParameter.get();
        return StringUtils.equals(rawType, variable.getType());
    }

    /**
     * Add the new parameter.
     *
     * @param vars the vars of the parameter.
     */
    @FxThread
    private void addParameter(@NotNull VarTable vars) {

        var node = getNode();
        var parameters = (SndParameters) node.getElement();

        var name = vars.getString(PROP_NAME);
        var GlslType = vars.getEnum(PROP_TYPE, GlslType.class);

        var variable = new ShaderNodeVariable(GlslType.getRawType(), name);

        var changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new AddSndParameterOperation(parameters, variable));
    }
}
