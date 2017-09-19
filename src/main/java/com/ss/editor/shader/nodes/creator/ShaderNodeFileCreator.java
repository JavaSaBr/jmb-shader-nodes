package com.ss.editor.shader.nodes.creator;

import static com.ss.editor.util.EditorUtil.getAssetFile;
import static com.ss.editor.util.EditorUtil.toAssetPath;
import static com.ss.rlib.util.ObjectUtils.notNull;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import com.jme3.renderer.Caps;
import com.jme3.renderer.Renderer;
import com.jme3.shader.Shader.ShaderType;
import com.ss.editor.FileExtensions;
import com.ss.editor.annotation.FXThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.extension.property.EditablePropertyType;
import com.ss.editor.plugin.api.file.creator.GenericFileCreator;
import com.ss.editor.plugin.api.property.PropertyDefinition;
import com.ss.editor.ui.component.creator.FileCreatorDescription;
import com.ss.editor.util.EditorUtil;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.VarTable;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

/**
 * The implementation of creator to create new j3sn file.
 *
 * @author JavaSaBr
 */
public class ShaderNodeFileCreator extends GenericFileCreator {

    @NotNull
    private static final String PROP_TYPE = "type";

    @NotNull
    private static final String PROP_GLSL_VERSION = "glslVersion";

    @NotNull
    private static final String PROP_SHADER_NODE_NAME = "shaderNodeName";

    /**
     * The description of this creator.
     */
    @NotNull
    public static final FileCreatorDescription DESCRIPTION = new FileCreatorDescription();

    static {
        DESCRIPTION.setFileDescription("New shader nodes file");
        DESCRIPTION.setConstructor(ShaderNodeFileCreator::new);
    }

    /**
     * The list of available GLSL languages.
     */
    @NotNull
    private static final Array<String> AVAILABLE_GLSL;

    /**
     * The list of available shader node types.
     */
    @NotNull
    private static final Array<String> AVAILABLE_TYPES;

    /**
     * The template of shader node file.
     */
    @NotNull
    private static final String SN_TEMPLATE;

    static {
        AVAILABLE_GLSL = ArrayFactory.newArray(String.class);
        AVAILABLE_TYPES = ArrayFactory.newArray(String.class);
        AVAILABLE_TYPES.add(ShaderType.Vertex.name());
        AVAILABLE_TYPES.add(ShaderType.Fragment.name());

        final Renderer renderer = EDITOR.getRenderer();

        final EnumSet<Caps> caps = renderer.getCaps();
        caps.stream().filter(cap -> cap.name().startsWith("GLSL"))
                .map(Enum::name)
                .sorted(StringUtils::compareIgnoreCase)
                .forEach(AVAILABLE_GLSL::add);

        final InputStream snResource = ShaderNodesProjectFileCreator.class
                .getResourceAsStream("/com/ss/editor/shader/nodes/template/ShaderNodeTemplate.j3sn");

        SN_TEMPLATE = FileUtils.read(snResource);
    }

    @Override
    @FromAnyThread
    protected @NotNull Array<PropertyDefinition> getPropertyDefinitions() {

        final Array<PropertyDefinition> result = ArrayFactory.newArray(PropertyDefinition.class);
        result.add(new PropertyDefinition(EditablePropertyType.STRING,
                "First shader name", PROP_SHADER_NODE_NAME, "newShaderNode"));
        result.add(new PropertyDefinition(EditablePropertyType.STRING_FROM_LIST,
                "Type", PROP_TYPE, ShaderType.Vertex.name(), AVAILABLE_TYPES));
        result.add(new PropertyDefinition(EditablePropertyType.STRING_FROM_LIST,
                "Language", PROP_GLSL_VERSION, Caps.GLSL150.name(), AVAILABLE_GLSL));

        return result;
    }

    @Override
    @FromAnyThread
    protected @NotNull String getFileExtension() {
        return FileExtensions.JME_SHADER_NODE;
    }

    @Override
    @FromAnyThread
    protected @NotNull String getTitleText() {
        return "Creating shader nodes file";
    }

    @Override
    @FXThread
    protected void processOk() {
        super.processOk();

        final Path shaderNodeFile = notNull(getFileToCreate());
        final Path folder = shaderNodeFile.getParent();

        final VarTable vars = getVars();
        final String shaderNodeName = vars.getString(PROP_SHADER_NODE_NAME);
        final Caps glslVersion = vars.getEnum(PROP_GLSL_VERSION, Caps.class);
        final ShaderType type = vars.getEnum(PROP_TYPE, ShaderType.class);

        final Path shaderFile = folder.resolve(shaderNodeName + "." + type.getExtension());
        final Path assetShaderFile = notNull(getAssetFile(shaderFile));
        final String assetShaderPath = toAssetPath(assetShaderFile);

        String result = SN_TEMPLATE.replace("${name}", shaderNodeName);
        result = result.replace("${type}", type.name());
        result = result.replace("${glsl}", glslVersion.name());
        result = result.replace("${shader_path}", assetShaderPath);

        try (final PrintWriter out = new PrintWriter(Files.newOutputStream(shaderNodeFile, WRITE, TRUNCATE_EXISTING, CREATE))) {
            out.print(result);
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
            return;
        }

        try (final PrintWriter out = new PrintWriter(Files.newOutputStream(shaderFile, WRITE, TRUNCATE_EXISTING, CREATE))) {
            out.print("void main() {\n\n}");
        } catch (final IOException e) {
            EditorUtil.handleException(LOGGER, this, e);
        }
    }
}
