package com.ss.editor.shader.nodes.ui.control.tree.action;

import static com.ss.editor.extension.property.EditablePropertyType.STRING;
import static com.ss.editor.extension.property.EditablePropertyType.STRING_FROM_LIST;
import static com.ss.editor.shader.nodes.ui.component.creator.ShaderNodeDefinitionsFileCreator.AVAILABLE_TYPES;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.shader.Shader.ShaderType;
import com.jme3.shader.ShaderNodeDefinition;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.model.undo.editor.ChangeConsumer;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.model.shader.node.definition.SndList;
import com.ss.editor.shader.nodes.ui.control.tree.operation.AddSndOperation;
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

import java.util.ArrayList;

/**
 * The action to add new shader node definition.
 *
 * @author JavaSaBr
 */
public class AddSndAction extends AbstractNodeAction<ChangeConsumer> {

    @NotNull
    private static final String PROP_NAME = "name";

    @NotNull
    private static final String PROP_TYPE = "type";

    public AddSndAction(@NotNull NodeTree<?> nodeTree, @NotNull TreeNode<?> node) {
        super(nodeTree, node);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return PluginMessages.ACTION_ADD_SHADER_NODE_DEFINITION;
    }

    @Override
    @FxThread
    protected @Nullable Image getIcon() {
        return Icons.ADD_16;
    }

    @Override
    @FxThread
    protected void process() {
        super.process();

        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(STRING, Messages.MODEL_PROPERTY_NAME, PROP_NAME, "newDefinition"));
        definitions.add(new PropertyDefinition(STRING_FROM_LIST, Messages.MODEL_PROPERTY_TYPE, PROP_TYPE,
                ShaderType.Vertex.name(), AVAILABLE_TYPES));

        var dialog = new GenericFactoryDialog(definitions, this::addDefinition, this::validate);
        dialog.setTitle(getName());
        dialog.show();
    }

    /**
     * Validate the variables.
     *
     * @param vars the vars of the definition.
     */
    @FxThread
    private boolean validate(@NotNull VarTable vars) {

        var name = vars.getString(PROP_NAME);

        var node = getNode();
        var element = (SndList) node.getElement();

        return element.getDefinitions().stream()
                .noneMatch(definition -> definition.getName().equals(name));
    }

    /**
     * Add a new shader node definition.
     *
     * @param vars the vars of the definition.
     */
    @FxThread
    private void addDefinition(@NotNull VarTable vars) {

        var definitionName = vars.getString(PROP_NAME);
        var type = vars.getEnum(PROP_TYPE, ShaderType.class);

        var node = getNode();
        var element = (SndList) node.getElement();

        var definition = new ShaderNodeDefinition();
        definition.setName(definitionName);
        definition.setType(type);
        definition.setDocumentation("");
        definition.setInputs(new ArrayList<>());
        definition.setOutputs(new ArrayList<>());

        var changeConsumer = notNull(getNodeTree().getChangeConsumer());
        changeConsumer.execute(new AddSndOperation(element, definition));
    }
}
