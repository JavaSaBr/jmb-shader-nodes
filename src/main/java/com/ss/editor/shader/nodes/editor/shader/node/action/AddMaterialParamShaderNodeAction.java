package com.ss.editor.shader.nodes.editor.shader.node.action;

import static com.ss.editor.extension.property.EditablePropertyType.STRING;
import static com.ss.editor.extension.property.EditablePropertyType.STRING_FROM_LIST;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.shader.VarType;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.plugin.api.dialog.GenericFactoryDialog;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.shader.nodes.editor.ShaderNodesChangeConsumer;
import com.ss.editor.shader.nodes.editor.operation.add.AddMaterialParameterOperation;
import com.ss.editor.shader.nodes.editor.shader.ShaderNodesContainer;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * The action to add new material param.
 *
 * @author JavaSaBr
 */
public class AddMaterialParamShaderNodeAction extends ShaderNodeAction<MaterialDef> {

    @NotNull
    protected static final String PROP_NAME = "name";

    @NotNull
    protected static final String PROP_TYPE = "type";

    @NotNull
    protected static final String PROP_DEFAULT = "default";

    @NotNull
    protected static final EnumSet<VarType> TEXTURE_TYPES = EnumSet.of(VarType.Texture2D,
            VarType.Texture3D,
            VarType.TextureCubeMap
    );

    @NotNull
    private static Array<String> VAR_TYPES = ArrayFactory.newArray(String.class);

    static {
        for (final VarType varType : VarType.values()) {
            if (!TEXTURE_TYPES.contains(varType)) {
                VAR_TYPES.add(varType.name());
            }
        }
    }

    public AddMaterialParamShaderNodeAction(@NotNull final ShaderNodesContainer container,
                                            @NotNull final MaterialDef materialDef,
                                            @NotNull final Vector2f location) {
        super(container, materialDef, location);
    }

    @Override
    @FXThread
    protected @NotNull String getName() {
        return "Material parameter";
    }

    @Override
    @FXThread
    protected void process() {
        super.process();

        final Array<PropertyDefinition> definitions = getDefinitions();

        final GenericFactoryDialog dialog = new GenericFactoryDialog(definitions, this::addParam, this::validate);
        dialog.show();
    }

    /**
     * Get the list of required properties.
     *
     * @return the list of required properties.
     */
    @FXThread
    protected @NotNull Array<PropertyDefinition> getDefinitions() {

        final Array<String> varTypes = getVarTypes();
        final Array<PropertyDefinition> definitions = ArrayFactory.newArray(PropertyDefinition.class);
        definitions.add(new PropertyDefinition(STRING, "name", PROP_NAME, getNewParameterName()));
        definitions.add(new PropertyDefinition(STRING_FROM_LIST, "type", PROP_TYPE, varTypes.first(), varTypes));

        if (needDefaultValue()) {
            definitions.add(new PropertyDefinition(STRING, "default", PROP_DEFAULT, ""));
        }

        return definitions;
    }

    /**
     * Get the default name of a new parameter.
     *
     * @return the default name of a new parameter.
     */
    @FromAnyThread
    protected @NotNull String getNewParameterName() {
        return "FloatValue";
    }

    /**
     * @return true if need to add default value property.
     */
    @FromAnyThread
    protected boolean needDefaultValue() {
        return true;
    }

    /**
     * Get the list of variable types.
     *
     * @return the list of variable types.
     */
    @FromAnyThread
    protected @NotNull Array<String> getVarTypes() {
        return VAR_TYPES;
    }

    /**
     * Add the new material parameter.
     *
     * @param vars the variables.
     */
    @FXThread
    protected void addParam(@NotNull final VarTable vars) {

        final ShaderNodesContainer container = getContainer();
        final ShaderNodesChangeConsumer changeConsumer = container.getChangeConsumer();
        final MaterialDef materialDef = getObject();

        final String name = vars.getString(PROP_NAME);
        final VarType varType = vars.getEnum(PROP_TYPE, VarType.class);
        final String defaultValue = vars.getString(PROP_DEFAULT, "");

        final MatParam matParam = new MatParam(varType, name, null);

        if (!defaultValue.isEmpty()) {
            switch (varType) {
                case Boolean:
                    matParam.setValue(vars.getBoolean(PROP_DEFAULT));
                    break;
                case Int:
                    matParam.setValue(vars.getInteger(PROP_DEFAULT));
                    break;
                case Float:
                    matParam.setValue(vars.getFloat(PROP_DEFAULT));
                    break;
                case Vector4:
                    float[] array = vars.getFloatArray(PROP_DEFAULT, " ");
                    matParam.setValue(new Vector4f(array[0], array[1], array[2], array[3]));
                    break;
                case Vector3:
                    array = vars.getFloatArray(PROP_DEFAULT, " ");
                    matParam.setValue(new Vector3f(array[0], array[1], array[2]));
                    break;
                case Vector2:
                    array = vars.getFloatArray(PROP_DEFAULT, " ");
                    matParam.setValue(new Vector2f(array[0], array[1]));
                    break;
                case FloatArray:
                    array = vars.getFloatArray(PROP_DEFAULT, " ");
                    matParam.setValue(array);
                    break;
                case IntArray:
                    final int[] intArray = vars.getIntegerArray(PROP_DEFAULT, " ");
                    matParam.setValue(intArray);
                    break;
            }
        }

        changeConsumer.execute(new AddMaterialParameterOperation(materialDef, matParam, getLocation()));
    }

    /**
     * Validate inputs from the dialog.
     *
     * @param vars the variables.
     * @return true if the values are valid.
     */
    @FXThread
    protected boolean validate(@NotNull final VarTable vars) {
        final String name = vars.getString(PROP_NAME);
        return getObject().getMaterialParam(name) == null;
    }
}
