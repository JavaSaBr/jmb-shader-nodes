package com.ss.editor.shader.nodes.ui.component.shader.nodes.action.add;

import static com.ss.editor.shader.nodes.ui.component.shader.nodes.main.AttributeShaderNodeElement.NAMESPACE;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.*;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.scene.VertexBuffer;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add.AddAttributeOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The action to add a new vertex attribute.
 *
 * @author JavaSaBr
 */
public class AddAttributeShaderNodeAction extends AddTechniqueDefParameterShaderNodeAction {

    @NotNull
    private static final Array<String> ATTRIBUTE_TYPES = ArrayFactory.newArray(String.class);

    static {
        for (final VertexBuffer.Type attribute : VertexBuffer.Type.values()) {

            if (attribute == VertexBuffer.Type.Position) {
                continue;
            }

            ATTRIBUTE_TYPES.add("in" + attribute.name());
        }
    }
    public AddAttributeShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                        @NotNull final TechniqueDef techniqueDef, @NotNull final Vector2f location) {
        super(container, techniqueDef, location);

        final ShaderGenerationInfo info = techniqueDef.getShaderGenerationInfo();
        final List<ShaderNodeVariable> attributes = info.getAttributes();

        ATTRIBUTE_TYPES.stream()
                .filter(name -> !containsByNN(attributes, name, NAMESPACE))
                .forEach(getAvailable()::add);
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return PluginMessages.VERTEX_ATTRIBUTE;
    }

    @Override
    @FxThread
    protected @NotNull String getDialogTitle() {
        return PluginMessages.ACTION_ADD_VERTEX_ATTRIBUTE_TITLE;
    }

    @Override
    @FxThread
    protected void addParameter(@NotNull final VarTable vars) {

        final ShaderNodesContainer container = getContainer();
        final ShaderNodesChangeConsumer changeConsumer = container.getChangeConsumer();
        final TechniqueDef techniqueDef = getObject();

        final String name = vars.getString(PROP_NAME);
        final String attributeUIType = getAttributeUiType(VertexBuffer.Type.valueOf(name.substring(2, name.length())));
        final String glslType = uiTypeToType(attributeUIType);
        final ShaderNodeVariable variable = new ShaderNodeVariable(glslType, NAMESPACE, name, null, "");

        changeConsumer.execute(new AddAttributeOperation(techniqueDef, variable, getLocation()));
    }
}
