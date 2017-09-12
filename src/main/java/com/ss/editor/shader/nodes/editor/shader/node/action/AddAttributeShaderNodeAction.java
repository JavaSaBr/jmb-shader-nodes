package com.ss.editor.shader.nodes.editor.shader.node.action;

import static com.ss.editor.shader.nodes.editor.shader.node.main.AttributeShaderNodeElement.NAMESPACE;
import static com.ss.editor.shader.nodes.util.ShaderNodeUtils.*;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.math.Vector2f;
import com.jme3.scene.VertexBuffer;
import com.jme3.shader.ShaderNodeVariable;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.operation.add.AddAttributeOperation;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
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
            ATTRIBUTE_TYPES.add(attribute.name());
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
    @FXThread
    protected @NotNull String getName() {
        return "Vertex Attribute";
    }

    @Override
    @FXThread
    protected void addParameter(@NotNull final VarTable vars) {

        final ShaderNodesContainer container = getContainer();
        final ShaderNodesChangeConsumer changeConsumer = container.getChangeConsumer();
        final TechniqueDef techniqueDef = getObject();

        final String name = vars.getString(PROP_NAME);
        final String attributeUIType = getAttributeUIType(VertexBuffer.Type.valueOf(name));
        final String glslType = uiTypeToType(attributeUIType);
        final ShaderNodeVariable variable = new ShaderNodeVariable(glslType, NAMESPACE, name, null, "in");

        changeConsumer.execute(new AddAttributeOperation(techniqueDef, variable, getLocation()));
    }
}
