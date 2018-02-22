package com.ss.editor.shader.nodes.ui.component.shader.nodes.action.add;

import static com.ss.editor.extension.property.EditablePropertyType.ENUM;
import com.jme3.material.MatParamTexture;
import com.jme3.material.MaterialDef;
import com.jme3.math.Vector2f;
import com.jme3.shader.VarType;
import com.jme3.texture.image.ColorSpace;
import com.ss.editor.Messages;
import com.ss.editor.annotation.FxThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.PluginMessages;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.ShaderNodesContainer;
import com.ss.editor.shader.nodes.ui.component.shader.nodes.operation.add.AddMaterialParameterOperation;
import com.ss.editor.shader.nodes.ui.component.editor.ShaderNodesChangeConsumer;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

/**
 * The action to add new material texture param.
 *
 * @author JavaSaBr
 */
public class AddMaterialTextureShaderNodeAction extends AddMaterialParamShaderNodeAction {

    @NotNull
    private static final String PROP_COLOR_SPACE = "colorSpace";

    @NotNull
    private static Array<String> VAR_TYPES = ArrayFactory.newArray(String.class);

    static {
        for (final VarType varType : TEXTURE_TYPES) {
            VAR_TYPES.add(varType.name());
        }
    }

    public AddMaterialTextureShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                              @NotNull final MaterialDef materialDef,
                                              @NotNull final Vector2f location) {
        super(container, materialDef, location);
    }

    @Override
    protected boolean needDefaultValue() {
        return false;
    }

    @Override
    @FxThread
    protected @NotNull String getName() {
        return PluginMessages.MATERIAL_TEXTURE;
    }

    @Override
    @FxThread
    protected @NotNull Array<PropertyDefinition> getDefinitions() {
        final Array<PropertyDefinition> definitions = super.getDefinitions();
        definitions.add(new PropertyDefinition(ENUM, Messages.MODEL_PROPERTY_COLOR_SPACE, PROP_COLOR_SPACE, ColorSpace.Linear));
        return definitions;
    }

    @Override
    @FromAnyThread
    protected @NotNull String getNewParameterName() {
        return "TextureValue";
    }

    @Override
    @FromAnyThread
    protected @NotNull Array<String> getVarTypes() {
        return VAR_TYPES;
    }

    @Override
    @FxThread
    protected void addParam(@NotNull final VarTable vars) {

        final ShaderNodesContainer container = getContainer();
        final ShaderNodesChangeConsumer changeConsumer = container.getChangeConsumer();
        final MaterialDef materialDef = getObject();

        final String name = vars.getString(PROP_NAME);
        final VarType varType = vars.getEnum(PROP_TYPE, VarType.class);
        final ColorSpace colorSpace = vars.getEnum(PROP_COLOR_SPACE, ColorSpace.class);

        final MatParamTexture matParam = new MatParamTexture(varType, name, null, colorSpace);
        changeConsumer.execute(new AddMaterialParameterOperation(materialDef, matParam, getLocation()));
    }
}
