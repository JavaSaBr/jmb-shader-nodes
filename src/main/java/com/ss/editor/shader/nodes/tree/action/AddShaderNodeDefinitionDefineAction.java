package com.ss.editor.shader.nodes.tree.action;

import static com.ss.editor.extension.property.EditablePropertyType.STRING;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefine;
import com.ss.editor.shader.nodes.model.shader.node.definition.ShaderNodeDefinitionDefines;
import com.ss.editor.shader.nodes.tree.operation.AddShaderNodeDefinitionDefineOperation;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.control.tree.NodeTree;
import com.ss.editor.ui.control.tree.action.AbstractNodeAction;
import com.ss.editor.ui.control.tree.node.TreeNode;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The action to add new define.
 *
 * @author JavaSaBr
 */
public class AddShaderNodeDefinitionDefineAction extends AbstractNodeAction<ChangeConsumer> {

    @NotNull
    private static final String PROP_NAME = "name";

    public AddShaderNodeDefinitionDefineAction(@NotNull final NodeTree<?> nodeTree, @NotNull final TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return "Add define";
    }

    @Override
    @FXThread
    protected @Nullable Image getIcon() {
        return Icons.ADD_16;
    }

    @Override
    @FXThread
    protected void process() {
        super.process();

        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(STRING, Messages.MODEL_PROPERTY_NAME, PROP_NAME, "NEW_DEFINE"));

        final GenericFactoryDialog dialog = new GenericFactoryDialog(definitions, this::addDefine, this::validate);
        dialog.setTitle(getName());
        dialog.show();
    }

    /**
     * Validate the variables.
     *
     * @param vars the vars of the define.
     */
    @FXThread
    private boolean validate(@NotNull final VarTable vars) {

        if (!vars.has(PROP_NAME)) {
            return false;
        }

        final TreeNode<?> node = getNode();
        final ShaderNodeDefinitionDefines localVariables = (ShaderNodeDefinitionDefines) node.getElement();
        final ShaderNodeDefinition definition = localVariables.getDefinition();
        final String name = vars.getString(PROP_NAME);
        return !definition.getDefines().contains(name);
    }

    /**
     * Add a new define.
     *
     * @param vars the vars of the define.
     */
    @FXThread
    private void addDefine(@NotNull final VarTable vars) {

        final String name = vars.getString(PROP_NAME);

        final TreeNode<?> node = getNode();
        final ShaderNodeDefinitionDefines defines = (ShaderNodeDefinitionDefines) node.getElement();
        final ShaderNodeDefinition definition = defines.getDefinition();
        final ShaderNodeDefinitionDefine define = new ShaderNodeDefinitionDefine(definition, name);

        final ChangeConsumer changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new AddShaderNodeDefinitionDefineOperation(defines, define));
    }
}
