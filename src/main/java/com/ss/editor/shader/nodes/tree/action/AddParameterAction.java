package com.ss.editor.shader.nodes.tree.action;

import static com.ss.editor.extension.property.EditablePropertyType.ENUM;
import static com.ss.editor.extension.property.EditablePropertyType.STRING;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeDefinition;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeParameters;
import com.ss.editor.shader.nodes.tree.operation.AddParameterOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.editor.util.GLSLType;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The action to add new parameter.
 *
 * @author JavaSaBr
 */
public abstract class AddParameterAction extends AbstractNodeAction<ChangeConsumer> {

    @NotNull
    private static final String PROP_NAME = "name";

    @NotNull
    private static final String PROP_TYPE = "type";

    public AddParameterAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return "Add parameter";
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.ADD_16;
    }

    /**
     * Get the list of current parameters.
     *
     * @param definition the definition.
     * @return the list of current parameters.
     */
    @FXThread
    protected abstract @NotNull List<ShaderNodeVariable> getCurrentParameters(
            @NotNull final ShaderNodeDefinition definition);

    /**
     * Get the list of opposite parameters.
     *
     * @param definition the definition.
     * @return the list of opposite parameters.
     */
    @FXThread
    protected abstract @NotNull List<ShaderNodeVariable> getOppositeParameters(
            @NotNull final ShaderNodeDefinition definition);

    @Override
    @FXThread
    protected void process() {
        super.process();

        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(STRING, "Name", PROP_NAME, "newVar"));
        definitions.add(new PropertyDefinition(ENUM, "Type", PROP_TYPE, GLSLType.FLOAT));

        final GenericFactoryDialog dialog = new GenericFactoryDialog(definitions, this::addParameter, this::validate);
        dialog.show();
    }

    /**
     * Validate the variables.
     *
     * @param vars the vars of the new parameter.
     */
    @FXThread
    private boolean validate(@NotNull final VarTable vars) {

        final TreeNode<?> node = getNode();
        final ShaderNodeParameters parameters = (ShaderNodeParameters) node.getElement();
        final ShaderNodeDefinition definition = parameters.getDefinition();

        final String name = vars.getString(PROP_NAME);
        final boolean exists = getCurrentParameters(definition).stream()
                .anyMatch(variable -> variable.getName().equals(name));

        if (exists) {
            return false;
        }

        final GLSLType glslType = vars.getEnum(PROP_TYPE, GLSLType.class);
        final String rawType = glslType.getRawType();

        final List<ShaderNodeVariable> oppositeParameters = getOppositeParameters(definition);
        final Optional<ShaderNodeVariable> oppositeParameter = oppositeParameters.stream()
                .filter(variable -> variable.getName().equals(name))
                .findAny();

        if (!oppositeParameter.isPresent()) {
            return true;
        }

        final ShaderNodeVariable variable = oppositeParameter.get();
        return StringUtils.equals(rawType, variable.getType());
    }

    /**
     * Add the new parameter.
     *
     * @param vars the vars of the parameter.
     */
    @FXThread
    private void addParameter(@NotNull final VarTable vars) {

        final TreeNode<?> node = getNode();
        final ShaderNodeParameters parameters = (ShaderNodeParameters) node.getElement();

        final String name = vars.getString(PROP_NAME);
        final GLSLType glslType = vars.getEnum(PROP_TYPE, GLSLType.class);

        final ShaderNodeVariable variable = new ShaderNodeVariable(glslType.getRawType(), name);

        final ChangeConsumer changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new AddParameterOperation(parameters, variable));
    }
}
