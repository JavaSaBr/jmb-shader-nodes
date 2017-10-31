package com.jme3.shader.glsl;

import static com.jme3.shader.glsl.parser.ast.util.ASTUtils.findAllByType;
import static java.lang.System.getProperty;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.cache.AssetCache;
import com.jme3.material.ShaderGenerationInfo;
import com.jme3.material.TechniqueDef;
import com.jme3.shader.*;
import com.jme3.shader.Shader.ShaderType;
import com.jme3.shader.glsl.parser.GLSLParser;
import com.jme3.shader.glsl.parser.ast.ASTNode;
import com.jme3.shader.glsl.parser.ast.NameASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.ExternalFieldDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.FileDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.LocalVarDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.declaration.MethodDeclarationASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.ExtensionPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.preprocessor.ImportPreprocessorASTNode;
import com.jme3.shader.glsl.parser.ast.util.ASTUtils;
import com.jme3.shader.glsl.parser.ast.value.DefineValueASTNode;
import com.jme3.shader.glsl.parser.ast.value.StringValueASTNode;
import com.jme3.shader.plugins.ShaderAssetKey;
import com.ss.editor.FileExtensions;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base implementation of a shader generator with using AST GLSL to generate a result shader.
 *
 * @author JavaSaBr
 */
public abstract class AstShaderGenerator extends Glsl100ShaderGenerator {

    public static final String PROP_USE_CASE = "AstShaderGenerator.useCache";

    public static final String SD_DEF_IMPORTS = "Imports";
    public static final String SD_DEF_DEFINES = "Defines";

    private static final boolean USE_AST_CACHE;

    static {
        USE_AST_CACHE = Boolean.parseBoolean(getProperty(PROP_USE_CASE, "true"));
    }

    protected static final char[] EMPTY_CHARS = new char[0];

    private class ImportedShaderKey extends AssetKey<Reader> {

        private ImportedShaderKey(final String name) {
            super(name);
        }

        @Override
        public Class<? extends AssetCache> getCacheType() {
            return null;
        }
    }

    /**
     * Calculate the indent using space characters.
     *
     * @param level the level.
     * @return the result indent.
     */
    protected static char[] getIndent(final int level) {

        if (level == 0) {
            return EMPTY_CHARS;
        }

        final int characters = level * 4;
        final char[] result = new char[characters];

        for (int i = 0; i < result.length; i++) {
            result[i] = ' ';
        }

        return result;
    }

    /**
     * The thread local state of this generator.
     */
    private static final ThreadLocal<AstShaderGeneratorState> LOCAL_STATE = new ThreadLocal<AstShaderGeneratorState>() {

        @Override
        protected AstShaderGeneratorState initialValue() {
            return new AstShaderGeneratorState();
        }
    };

    public AstShaderGenerator(final AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    protected void indent() {
        final AstShaderGeneratorState state = LOCAL_STATE.get();
        state.setIndent(state.getIndent() + 1);
    }

    @Override
    protected void unIndent() {
        final AstShaderGeneratorState state = LOCAL_STATE.get();
        if (state.getIndent() < 0) return;
        state.setIndent(state.getIndent() - 1);
    }

    @Override
    protected void appendIndent(final StringBuilder source) {
        final AstShaderGeneratorState state = LOCAL_STATE.get();
        source.append(getIndent(state.getIndent()));
    }

    @Override
    public void initialize(final TechniqueDef techniqueDef) {
        super.initialize(techniqueDef);

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        state.setTechniqueDef(techniqueDef);
        state.setIndent(0);
        state.getImportedGlobalUniforms().clear();

        prepareShaderNodeSources(techniqueDef.getShaderNodes());
    }

    @Override
    public Shader generateShader(final String definesSourceCode) {

        final long time = System.currentTimeMillis();

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final TechniqueDef techniqueDef = state.getTechniqueDef();
        final Shader result = super.generateShader(definesSourceCode);

        // we need to add uniform bindings from imported shaders, because it can be unpresented in shader nodes.
        final List<UniformBinding> worldBindings = techniqueDef.getWorldBindings();
        final List<ExternalFieldDeclarationASTNode> globalUniforms = state.getImportedGlobalUniforms();

        ASTUtils.removeExists(globalUniforms, worldBindings);

        if (!globalUniforms.isEmpty()) {
            for (final ExternalFieldDeclarationASTNode field : globalUniforms) {

                final NameASTNode nameNode = field.getName();
                final String name = nameNode.getName();

                final UniformBinding binding = UniformBinding.valueOf(name.substring(2, name.length()));
                result.addUniformBinding(binding);
            }
        }

        System.out.println("Generated shader " + techniqueDef.getName() + ":" + (System.currentTimeMillis() - time));

        return result;
    }

    @Override
    protected String buildShader(final List<ShaderNode> shaderNodes, final ShaderGenerationInfo info,
                                 final ShaderType type) {

        if (type != ShaderType.Vertex && type != ShaderType.Fragment) {
            return null;
        }

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = state.getShaderNodeSources();
        final List<ExtensionPreprocessorASTNode> extensionNodes = state.getExtensionNodes();
        extensionNodes.clear();

        final List<ImportPreprocessorASTNode> importNodes = state.getImportNodes();
        importNodes.clear();

        findImportsAndExtensions(shaderNodes, type, shaderNodeSources, extensionNodes, importNodes);

        ASTUtils.removeExtensionDuplicates(extensionNodes);
        ASTUtils.removeImportDuplicates(importNodes);

        final StringBuilder headerSource = ASTUtils.clear(state.getHeaderSource());
        final StringBuilder importsSource = ASTUtils.clear(state.getImportsSource());
        final StringBuilder uniformsSource = ASTUtils.clear(state.getUniformsSource());
        final StringBuilder methodsSource = ASTUtils.clear(state.getMethodsSource());
        final StringBuilder mainSource = ASTUtils.clear(state.getMainSource());

        generateExtensions(extensionNodes, headerSource);

        final List<ExternalFieldDeclarationASTNode> importedUniforms = state.getImportedUnforms();
        importedUniforms.clear();

        generateImports(importNodes, importedUniforms, importsSource);

        ASTUtils.copyGlobalUniforms(importedUniforms, state.getImportedGlobalUniforms());

        generateUniforms(uniformsSource, info, type);

        if (type == ShaderType.Vertex) {
            generateAttributes(uniformsSource, info);
        }

        generateVaryings(uniformsSource, info, type);
        generateMethods(shaderNodes, type, methodsSource);
        generateStartOfMainSection(mainSource, info, type);
        generateDeclarationAndMainBody(shaderNodes, null, mainSource, info, type);
        generateEndOfMainSection(mainSource, info, type);
        generateVarDefines(headerSource, state.getResultDefines());
        generateCompatibilityDefines(headerSource, type);
        generateShaderNodeHeaders(shaderNodes, info, type, headerSource);

        final StringBuilder result = new StringBuilder();

        if (headerSource.length() > 0) {
            result.append(headerSource).append('\n');
        }

        if (importsSource.length() > 0) {
            result.append(importsSource);
        }

        if (uniformsSource.length() > 0) {
            result.append(uniformsSource).append('\n');
        }

        if (methodsSource.length() > 0) {
            result.append(methodsSource).append('\n');
        }

        return result.append(mainSource).toString();
    }

    /**
     * Generates shader nodes headers.
     *
     * @param shaderNodes  the list of shader nodes.
     * @param info         the generating information.
     * @param type         the shader type.
     * @param headerSource the header source.
     */
    private void generateShaderNodeHeaders(final List<ShaderNode> shaderNodes, final ShaderGenerationInfo info,
                                           final ShaderType type, final StringBuilder headerSource) {

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = state.getShaderNodeSources();
        final List<DefineValueASTNode> defineValueNodes = state.getDefineValueNodes();
        final List<String> unusedNodes = info.getUnusedNodes();

        for (final ShaderNode shaderNode : shaderNodes) {

            if (unusedNodes.contains(shaderNode.getName())) {
                continue;
            }

            final ShaderNodeDefinition definition = shaderNode.getDefinition();
            if (definition.getType() != type) {
                continue;
            }

            final FileDeclarationASTNode shaderFile = shaderNodeSources.get(shaderNode);
            final List<ASTNode> children = shaderFile.getChildren();

            for (final ASTNode child : children) {

                if (child instanceof MethodDeclarationASTNode) {
                    continue;
                }

                defineValueNodes.clear();
                findAllByType(child, defineValueNodes, DefineValueASTNode.class);

                final String code = updateDefineNames(shaderNode, child.getText(), defineValueNodes);

                headerSource.append(code).append('\n').append('\n');
            }
        }
    }

    /**
     * Generates compatibility defines.
     *
     * @param headerSource the header source.
     * @param type         the shader type.
     */
    protected void generateCompatibilityDefines(final StringBuilder headerSource, final ShaderType type) {
    }

    /**
     * Generates variable defines.
     *
     * @param headerSource  the header source.
     * @param resultDefines the result defines list.
     */
    private void generateVarDefines(final StringBuilder headerSource, final List<String> resultDefines) {

        if (resultDefines.isEmpty()) {
            return;
        }

        for (final String define : resultDefines) {
            headerSource.append("#define ").append(define).append(" 1").append('\n');
        }

        headerSource.append('\n');
    }

    /**
     * Finds imports and extensionNodes from the shader nodes.
     *
     * @param shaderNodes       the shader nodes.
     * @param type              the current type.
     * @param shaderNodeSources the shader node sources.
     * @param extensionNodes    the extension nodes.
     * @param importNodes       the import nodes.
     */
    private void findImportsAndExtensions(final List<ShaderNode> shaderNodes, final ShaderType type,
                                          final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources,
                                          final List<ExtensionPreprocessorASTNode> extensionNodes,
                                          final List<ImportPreprocessorASTNode> importNodes) {

        for (final ShaderNode shaderNode : shaderNodes) {

            final ShaderNodeDefinition definition = shaderNode.getDefinition();
            if (definition.getType() != type) {
                continue;
            }

            final FileDeclarationASTNode fileDeclarationASTNode = shaderNodeSources.get(shaderNode);
            findAllByType(fileDeclarationASTNode, extensionNodes, ExtensionPreprocessorASTNode.class);
            findAllByType(fileDeclarationASTNode, importNodes, ImportPreprocessorASTNode.class);
        }
    }

    @Override
    protected void generateDeclarationAndMainBody(final List<ShaderNode> shaderNodes,
                                                  final StringBuilder sourceDeclaration, final StringBuilder source,
                                                  final ShaderGenerationInfo info, final ShaderType type) {

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = state.getShaderNodeSources();
        final List<LocalVarDeclarationASTNode> localVariables = state.getLocalVars();
        final List<MethodDeclarationASTNode> methods = state.getMethods();
        final List<String> unusedNodes = info.getUnusedNodes();

        final List<DefineValueASTNode> defineValueNodes = state.getDefineValueNodes();
        final List<String> definedVariables = state.getDefinedVariables();
        final List<String> resultDefines = state.getResultDefines();
        resultDefines.clear();

        for (final ShaderNode shaderNode : shaderNodes) {

            if (unusedNodes.contains(shaderNode.getName())) {
                continue;
            }

            final ShaderNodeDefinition definition = shaderNode.getDefinition();
            if (definition.getType() != type) {
                continue;
            }

            methods.clear();
            definedVariables.clear();
            defineValueNodes.clear();

            final FileDeclarationASTNode shaderFile = shaderNodeSources.get(shaderNode);
            findAllByType(shaderFile, methods, MethodDeclarationASTNode.class);
            findAllByType(shaderFile, defineValueNodes, DefineValueASTNode.class);

            ASTUtils.removeDefineValueDuplicates(defineValueNodes);
            ASTUtils.copyDefinedVariables(defineValueNodes, definedVariables);

            findAvailableDefinesToDefine(shaderNode, definedVariables, resultDefines);

            final MethodDeclarationASTNode mainMethod = findMainMethod(methods);

            if (mainMethod == null) {
                generateNodeMainSection(source, shaderNode, null, info);
                continue;
            }

            findAllByType(mainMethod, localVariables, LocalVarDeclarationASTNode.class);

            String methodBodySource = updateMethodCalls(shaderNode, mainMethod, methods);
            methodBodySource = updateLocalVarNames(shaderNode, methodBodySource, localVariables);
            methodBodySource = updateDefineNames(shaderNode, methodBodySource, defineValueNodes);

            generateNodeMainSection(source, shaderNode, methodBodySource, info);
        }
    }

    @Override
    protected void generateNodeMainSection(final StringBuilder source, final ShaderNode shaderNode, String nodeSource,
                                           final ShaderGenerationInfo info) {

        if (nodeSource == null) {
            comment(source, shaderNode, "Begin");
            comment(source, shaderNode, "End");
            return;
        }

        comment(source, shaderNode, "Begin");
        startCondition(shaderNode.getCondition(), source);

        final ShaderNodeDefinition definition = shaderNode.getDefinition();
        final List<String> declaredVariables = new ArrayList<>();

        for (final VariableMapping mapping : shaderNode.getInputMapping()) {

            final ShaderNodeVariable rightVariable = mapping.getRightVariable();
            final ShaderNodeVariable leftVariable = mapping.getLeftVariable();

            // Variables fed with a sampler matparam or world param are replaced by the matparam itself
            // It avoids issue with samplers that have to be uniforms.
            if (isWorldOrMaterialParam(rightVariable) && rightVariable.getType().startsWith("sampler")) {
                nodeSource = replace(nodeSource, leftVariable, rightVariable.getPrefix() + rightVariable.getName());
            } else {

                if (leftVariable.getType().startsWith("sampler")) {
                    throw new IllegalArgumentException("a Sampler must be a uniform");
                }

                map(mapping, source);
            }

            String newName = shaderNode.getName() + "_" + leftVariable.getName();

            if (!declaredVariables.contains(newName)) {
                nodeSource = replace(nodeSource, leftVariable, newName);
                declaredVariables.add(newName);
            }
        }

        for (final ShaderNodeVariable var : definition.getInputs()) {

            if (var.getDefaultValue() == null) {
                continue;
            }

            final String fullName = shaderNode.getName() + "_" + var.getName();

            if (declaredVariables.contains(fullName)) {
                continue;
            }

            final ShaderNodeVariable variable = new ShaderNodeVariable(var.getType(), shaderNode.getName(),
                    var.getName(), var.getMultiplicity());

            if (!isVarying(info, variable)) {
                declareVariable(source, variable, var.getDefaultValue(), true, null);
            }
            nodeSource = replaceVariableName(nodeSource, variable);
            declaredVariables.add(fullName);
        }

        for (final ShaderNodeVariable var : definition.getOutputs()) {

            if (declaredVariables.contains(shaderNode.getName() + "_" + var.getName())) {
                continue;
            }

            final ShaderNodeVariable variable = new ShaderNodeVariable(var.getType(), shaderNode.getName(),
                    var.getName(), var.getMultiplicity());

            if (!isVarying(info, variable)) {
                declareVariable(source, variable);
            }

            nodeSource = replaceVariableName(nodeSource, variable);
        }

        appendIndent(source);

        source.append(nodeSource);
        source.append('\n');

        for (final VariableMapping mapping : shaderNode.getOutputMapping()) {
            map(mapping, source);
        }

        endCondition(shaderNode.getCondition(), source);
        comment(source, shaderNode, "End");
    }

    @Override
    protected String replace(final String source, final ShaderNodeVariable var, final String newName) {
        return ASTUtils.replaceVar(source, var.getName(), newName);
    }

    /**
     * Updates the method calls from the main method.
     *
     * @param shaderNode the shader node.
     * @param mainMethod the main method.
     * @param methods    the list of all methods.
     * @return the updated source.
     */
    private String updateMethodCalls(final ShaderNode shaderNode, final MethodDeclarationASTNode mainMethod,
                                     final List<MethodDeclarationASTNode> methods) {

        String methodBodySource = mainMethod.getBody().getText();

        if (methods.size() < 2) {
            return methodBodySource;
        }

        for (final MethodDeclarationASTNode methodDeclaration : methods) {

            final NameASTNode methodName = methodDeclaration.getName();
            final String name = methodName.getName();

            if (name.equals("main")) {
                continue;
            }

            // replace calls of the declared methods.
            methodBodySource = ASTUtils.replaceMethod(methodBodySource, name, shaderNode.getName() + "_" + name);
        }

        return methodBodySource;
    }

    /**
     * Updates the names of local variables in the main method.
     *
     * @param shaderNode the shader node.
     * @param source     the current source.
     * @param localVars  the list of local variables.
     * @return the updated source.
     */
    private String updateLocalVarNames(final ShaderNode shaderNode, String source,
                                       final List<LocalVarDeclarationASTNode> localVars) {

        if (localVars.isEmpty()) {
            return source;
        }

        for (final LocalVarDeclarationASTNode localVar : localVars) {

            final NameASTNode methodName = localVar.getName();
            final String name = methodName.getName();

            // replace calls of the declared methods.
            source = ASTUtils.replaceVar(source, name, shaderNode.getName() + "_" + name);
        }

        return source;
    }

    /**
     * Calculate used define names in the shader source which need to define in the top of the result shader..
     *
     * @param shaderNode    the shader node.
     * @param definedVars   the defined vars.
     * @param resultDefines the result defines.
     */
    private void findAvailableDefinesToDefine(final ShaderNode shaderNode, final List<String> definedVars,
                                              final List<String> resultDefines) {

        if (definedVars.isEmpty()) {
            return;
        }

        final List<VariableMapping> inputMapping = shaderNode.getInputMapping();

        for (final String defineName : definedVars) {
            for (final VariableMapping mapping : inputMapping) {
                final ShaderNodeVariable variable = mapping.getLeftVariable();
                if (variable.getName().equals(defineName)) {
                    resultDefines.add(ASTUtils.toResultDefineVarName(shaderNode, defineName));
                    break;
                }
            }
        }
    }

    /**
     * Updates the define names in the source code.
     *
     * @param shaderNode       the shader node.
     * @param source           the current source.
     * @param defineValueNodes the define value nodes.
     * @return the updated source.
     */
    private String updateDefineNames(final ShaderNode shaderNode, String source,
                                     final List<DefineValueASTNode> defineValueNodes) {

        if (defineValueNodes.isEmpty()) {
            return source;
        }

        for (final DefineValueASTNode defineValueNode : defineValueNodes) {
            final String define = defineValueNode.getValue();
            if (ASTUtils.isShaderNodeDefine(define)) {
                source = ASTUtils.replaceVar(source, define, shaderNode.getName() + "_" + define);
            }
        }

        return source;
    }

    /**
     * Finds the main method.
     *
     * @param methods the list of methods.
     * @return the main method or null.
     */
    private MethodDeclarationASTNode findMainMethod(final List<MethodDeclarationASTNode> methods) {

        if (methods.isEmpty()) {
            return null;
        }

        for (final MethodDeclarationASTNode methodDeclaration : methods) {

            final NameASTNode methodName = methodDeclaration.getName();
            final String name = methodName.getName();

            if (name.equals("main")) {
                return methodDeclaration;
            }
        }

        return null;
    }

    /**
     * Generate all not main methods of all shader nodes.
     *
     * @param shaderNodes the shader nodes.
     * @param type        the shader type.
     * @param builder     the target builder.
     */
    protected void generateMethods(final List<ShaderNode> shaderNodes, final ShaderType type,
                                   final StringBuilder builder) {

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final List<MethodDeclarationASTNode> methods = state.getMethods();
        methods.clear();

        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = state.getShaderNodeSources();

        for (final ShaderNode shaderNode : shaderNodes) {

            if (shaderNode.getDefinition().getType() != type) {
                continue;
            }

            methods.clear();

            final FileDeclarationASTNode shaderFile = shaderNodeSources.get(shaderNode);
            findAllByType(shaderFile, methods, MethodDeclarationASTNode.class);

            if (methods.size() < 2) {
                continue;
            }

            for (final MethodDeclarationASTNode method : methods) {

                final NameASTNode name = method.getName();
                final String methodName = name.getName();

                if ("main".equals(methodName)) {
                    continue;
                }

                final String methodContent = method.getText();
                final String resultContent = methodContent.replace(methodName, shaderNode.getName() + "_" + methodName);

                builder.append(resultContent).append('\n');
            }
        }
    }

    /**
     * Generates all importNodes.
     *
     * @param importNodes      the list of import nodes.
     * @param importedUniforms the list of imported uniforms.
     * @param builder          the target builder.
     */
    protected void generateImports(final List<ImportPreprocessorASTNode> importNodes,
                                   final List<ExternalFieldDeclarationASTNode> importedUniforms,
                                   final StringBuilder builder) {

        if (importNodes.isEmpty()) {
            return;
        }

        for (final ImportPreprocessorASTNode importNode : importNodes) {
            final StringValueASTNode importValue = importNode.getValue();
            final FileDeclarationASTNode shaderFile = parseShaderSource(importValue.getValue());
            findAllByType(shaderFile, importedUniforms, ExternalFieldDeclarationASTNode.class);
            builder.append(shaderFile.getText()).append('\n');
        }

        builder.append('\n');
    }

    /**
     * Generates all extensionNodes.
     *
     * @param extensionNodes the list of extension nodes.
     * @param builder        the target builder.
     */
    protected void generateExtensions(final List<ExtensionPreprocessorASTNode> extensionNodes,
                                      final StringBuilder builder) {

        if (extensionNodes.isEmpty()) {
            return;
        }

        for (final ExtensionPreprocessorASTNode extension : extensionNodes) {
            builder.append(extension.getText()).append('\n');
        }

        builder.append('\n');
    }

    @Override
    protected void generateUniforms(final StringBuilder source, final ShaderGenerationInfo info,
                                    final ShaderType type) {
        switch (type) {
            case Vertex:
                generateUniforms(source, info.getVertexUniforms());
                break;
            case Fragment:
                generateUniforms(source, info.getFragmentUniforms());
                break;
        }
    }

    @Override
    protected void generateUniforms(final StringBuilder source, final List<ShaderNodeVariable> uniforms) {

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final List<ExternalFieldDeclarationASTNode> importedFields = state.getImportedUnforms();

        for (final ShaderNodeVariable var : uniforms) {
            if (isExist(var, importedFields)) continue;
            declareVariable(source, var, false, "uniform");
        }
    }

    @Override
    protected void generateAttributes(final StringBuilder source, final ShaderGenerationInfo info) {

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final List<ExternalFieldDeclarationASTNode> importedFields = state.getImportedUnforms();

        boolean inPosition = false;

        for (final ShaderNodeVariable var : info.getAttributes()) {
            if (var.getName().equals("inPosition")) {
                inPosition = true;
                var.setCondition(null);
                fixInPositionType(var);
                //keep track on the InPosition variable to avoid iterating through attributes again
                inPosTmp = var;
            }
            if (isExist(var, importedFields)) continue;
            declareAttribute(source, var);
        }

        if (!inPosition) {
            inPosTmp = new ShaderNodeVariable("vec3", "inPosition");
            if (isExist(inPosTmp, importedFields)) return;
            declareAttribute(source, inPosTmp);
        }
    }

    /**
     * Check of existing the variable in the imported shaders.
     *
     * @param variable       the variable.
     * @param importedFields the list of fields from imported shader.
     * @return true if the variable is exists.
     */
    private boolean isExist(final ShaderNodeVariable variable,
                            final List<ExternalFieldDeclarationASTNode> importedFields) {

        if (importedFields.isEmpty()) {
            return false;
        }

        final String name = variable.getName();
        final String prefix = variable.getPrefix();

        final int length = prefix.length() + name.length();

        for (final ExternalFieldDeclarationASTNode field : importedFields) {
            final NameASTNode nameASTNode = field.getName();
            final String fieldName = nameASTNode.getName();
            if (fieldName.length() != length || !fieldName.startsWith(prefix)) {
                continue;
            } else if (fieldName.endsWith(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Prepares the map with shader source path - parsed AST files.
     *
     * @param shaderNodes the list of shader nodes.
     */
    protected void prepareShaderNodeSources(final List<ShaderNode> shaderNodes) {

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final Map<ShaderNode, FileDeclarationASTNode> shaderNodeSources = state.getShaderNodeSources();
        shaderNodeSources.clear();

        for (final ShaderNode shaderNode : shaderNodes) {

            final ShaderNodeDefinition definition = shaderNode.getDefinition();

            final int index = findShaderIndexFromVersion(shaderNode, definition.getType());
            final String shaderSourcePath = definition.getShadersPath().get(index);

            shaderNodeSources.put(shaderNode, parseShaderSource(shaderSourcePath));
        }
    }

    /**
     * Parses the shader source by the shader source path.
     *
     * @param shaderSourcePath the path to the shader source file.
     * @return the parsed shader source as AST file.
     */
    protected FileDeclarationASTNode parseShaderSource(final String shaderSourcePath) {

        final AstShaderGeneratorState state = LOCAL_STATE.get();
        final Map<String, FileDeclarationASTNode> cache = state.getAstCache();
        final FileDeclarationASTNode cached = USE_AST_CACHE ? cache.get(shaderSourcePath) : null;

        if (cached != null) {
            return cached;
        }

        final Map<String, String> sourceMap;

        if (shaderSourcePath.endsWith(FileExtensions.GLSL_LIB)) {

            final StringBuilder builder = new StringBuilder();

            try (final Reader reader = assetManager.loadAsset(new ImportedShaderKey(shaderSourcePath))) {
                int ch;
                while ((ch = reader.read()) != -1) {
                    builder.append((char) ch);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            sourceMap = new HashMap<>();
            sourceMap.put("[main]", builder.toString());

        } else {
            sourceMap = (Map<String, String>)
                    assetManager.loadAsset(new ShaderAssetKey(shaderSourcePath, false));
        }

        final GLSLParser parser = GLSLParser.newInstance();
        final FileDeclarationASTNode result = parser.parseFileDeclaration(shaderSourcePath, sourceMap.get("[main]"));

        for (final Map.Entry<String, String> entry : sourceMap.entrySet()) {

            final String key = entry.getKey();
            if ("[main]".equals(key)) {
                continue;
            }

            final StringValueASTNode importValue = new StringValueASTNode();
            importValue.setValue(key);
            importValue.setText(key);

            final ImportPreprocessorASTNode importNode = new ImportPreprocessorASTNode();
            importNode.setValue(importValue);
            importNode.setText("#import \"" + key + "\"");

            result.getChildren().add(0, importNode);

            if (USE_AST_CACHE) {
                cache.put(key, parseShaderSource(entry.getValue()));
            }
        }

        if (USE_AST_CACHE) {
            cache.put(shaderSourcePath, result);
        }

        return result;
    }
}
