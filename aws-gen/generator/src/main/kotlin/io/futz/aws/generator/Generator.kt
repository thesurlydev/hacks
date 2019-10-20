package io.futz.aws.generator

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.futz.aws.model.CloudFormationResource
import io.futz.aws.model.NameValue
import io.futz.aws.parser.DocumentationParser
import io.futz.aws.parser.KeyParser
import java.io.File
import java.nio.file.Path
import java.util.*

private const val LIST = "List"
private const val MAP = "Map"
private const val DEFAULT_NULL = "null"
private const val DEFAULT_EMPTY_LIST = "emptyList()"
private const val DEFAULT_EMPTY_MAP = "emptyMap()"
private const val MUTABLE = "Mutable"
private const val TAG = "Tag"
private const val GEN_DIR = "generated/src/main/kotlin"

abstract class Documentation {
    val documentation: String? = null
}

data class Property(
    val required: Boolean = false,
    val primitiveType: String? = null,
    val primitiveItemType: String? = null,
    val updateType: String? = null,
    val type: String? = null,
    val itemType: String? = null
) :
    Documentation()

data class PropertyType(val properties: Map<String, Property> = mutableMapOf()) : Documentation()

data class Attribute(
    val primitiveItemType: String?,
    val type: String? = null,
    val updateType: String? = null,
    val required: Boolean = false,
    val primitiveType: String?
) : Documentation()

data class ResourceType(
    val attributes: Map<String, Attribute> = mutableMapOf(),
    val properties: Map<String, Property> = mutableMapOf()
) : Documentation()

data class Specification(
    val propertyTypes: Map<String, PropertyType> = mutableMapOf(),
    val resourceSpecificationVersion: String?,
    val resourceTypes: Map<String, ResourceType> = mutableMapOf()
)



fun getClassName(p: Property, packageName: String): TypeName? {

    val type = p.type
    val itemType = p.itemType
    val primitiveItemType = p.primitiveItemType
    val primitiveType = p.primitiveType

    return if (type != null && LIST == type) {
        return if (primitiveItemType != null) {
            if ("Json" == primitiveItemType) {
                // TODO will need to verify this synthesizes correctly; this is an edge case List<Json>
                List::class.asClassName().parameterizedBy(ClassName("io.futz.aws.model", NameValue::class.simpleName!!))
            } else {
                List::class.asClassName().parameterizedBy(ClassName("kotlin", primitiveItemType))
            }
        } else {
            if (itemType!! == TAG) {
                List::class.asClassName().parameterizedBy(ClassName("aws", itemType))
            } else {
                List::class.asClassName().parameterizedBy(ClassName(packageName, itemType))
            }
        }
    } else if (type != null && MAP == type) {
        return when {
            primitiveItemType != null -> Map::class.asClassName()
                .parameterizedBy(ClassName("kotlin", primitiveItemType), ClassName("kotlin", primitiveItemType))
            itemType != null -> Map::class.asClassName().parameterizedBy(
                String::class.asClassName(),
                ClassName(packageName, itemType)
            )
            else -> Map::class.asClassName().parameterizedBy(String::class.asClassName(), Any::class.asClassName())
        }
    } else if ("Double" == primitiveType) {
        Double::class.asClassName()
    } else if ("String" == primitiveType || "Json" == primitiveType || "Timestamp" == primitiveType) {
        String::class.asClassName()
    } else if ("Integer" == primitiveType) {
        Int::class.asClassName()
    } else if ("Boolean" == primitiveType) {
        Boolean::class.asClassName()
    } else if ("Long" == primitiveType) {
        Long::class.asClassName()
    } else if (MAP == primitiveType) {
        Map::class.asClassName().parameterizedBy(String::class.asClassName(), Any::class.asClassName())
    } else if (type != null) {
        ClassName(packageName, type)
    } else {
        System.err.println("Could not derive KClass from: $p")
        null
    }
}


fun main(args: Array<String>) {
    val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .registerKotlinModule()

    // see: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-resource-specification.html
    val f = File("/home/shane/projects/aws-generator/generator/src/test/resources/CloudFormationResourceSpecification-3.3.0.json")

    val spec = objectMapper.readValue<Specification>(f, Specification::class.java)
    val specVersion = spec.resourceSpecificationVersion!!

//    println(spec)

    val classMap = mutableMapOf<String, String>()

    // generate PropertyTypes
    val stack = Stack<Map.Entry<String, PropertyType>>()
    stack.addAll(spec.propertyTypes.entries)
    while (stack.isNotEmpty()) {
        val en = stack.pop()
        try {
            processPropertyTypeEntry(en, classMap)
        } catch (e: Exception) {
            System.err.println("err: $en")
            e.printStackTrace()
        }
    }

    // generate ResourceTypes
    val rStack = Stack<Map.Entry<String, ResourceType>>()
    rStack.addAll(spec.resourceTypes.entries)
    while (rStack.isNotEmpty()) {
        val en = rStack.pop()
        try {
            processResourceTypeEntry(specVersion, en, classMap)
        } catch (e: Exception) {
            System.err.println("err: $en")
            e.printStackTrace()
        }
    }
}


private fun processResourceTypeEntry(specVersion: String, entry: Map.Entry<String, ResourceType>, pMap: MutableMap<String, String>) {
    val parts = KeyParser().parse(entry.key)
    val packageName = parts[0]
    val className = parts[1]

    // make cloudFormationType available. e.g. "AWS::DynamoDB::Table"
    val cloudFormationTypeName = "cloudFormationType"
    val cloudFormationTypeTypeName = String::class.asTypeName()

    val primaryCtor = FunSpec.constructorBuilder()
    primaryCtor.addParameter(
        ParameterSpec.builder(cloudFormationTypeName, cloudFormationTypeTypeName)
            .defaultValue("%S", entry.key)
            .addModifiers(KModifier.PRIVATE)
            .build()
    )

    val properties = mutableListOf<PropertySpec>()
    properties.add(
        PropertySpec.builder(cloudFormationTypeName, cloudFormationTypeTypeName)
            .initializer(cloudFormationTypeName)
            .mutable(false).build()
    )


    val documentationLink = entry.value.documentation!!
    val docMap = DocumentationParser().parse(specVersion, documentationLink)

    val requiredParamSpecs = mutableListOf<ParameterSpec>()
    val resourceType = entry.value
    val props = resourceType.properties
    props.forEach { property ->
        val key = property.key
        val name = key.decapitalize()
        val prop = property.value
        val req = prop.required

        val mutable = when (prop.updateType) {
            MUTABLE -> true
            else -> false
        }

        val pMapKey = if (prop.type == LIST || prop.type == MAP) {
            prop.itemType
        } else {
            prop.type
        }

        val propPackageName = pMap[pMapKey] ?: packageName

        val cn = when {
            req -> getClassName(prop, propPackageName)
            else -> getClassName(prop, propPackageName)?.copy(nullable = true)
        }

        val paramSpec = ParameterSpec.builder(name, cn!!)
        when {
            !req ->
                if (prop.type != null) {
                    when {
                        prop.type == LIST -> paramSpec.defaultValue(DEFAULT_EMPTY_LIST)
                        prop.type == MAP -> paramSpec.defaultValue(DEFAULT_EMPTY_MAP)
                        else -> paramSpec.defaultValue(DEFAULT_NULL)
                    }
                } else {
                    paramSpec.defaultValue(DEFAULT_NULL)
                }
        }

        val propDoc = docMap[name.toLowerCase()]
        val kDoc = when {
            propDoc?.description != null -> {
                val starCount = propDoc.description!!.indexOf("/*")
                when {
                    starCount > -1 -> {
                        val escapedDesc = propDoc.description!!.replace("/*", "/\\*")
                        CodeBlock.of(escapedDesc)
                    }
                    else -> CodeBlock.of(propDoc.description!!)
                }
            }
            else -> null
        }

        val propSpec = PropertySpec.builder(name, cn)
        if (req) {
            val parameterSpec = paramSpec.build()
            requiredParamSpecs.add(parameterSpec)

            primaryCtor.addParameter(parameterSpec)

            propSpec.initializer(name).mutable(mutable)
            if (kDoc != null) {
                propSpec.addKdoc(kDoc)
            }
        } else {
            propSpec.mutable(true).initializer(DEFAULT_NULL)
            if (kDoc != null) {
                propSpec.addKdoc(kDoc)
            }
        }
        properties.add(propSpec.build())
    }

    val toCloudFormationResourceFunction = createToCloudFormationResourceFunction(properties)

    val typeSpecBuilder = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.DATA)
        .addProperties(properties)
        .primaryConstructor(primaryCtor.build())
        .addFunction(toCloudFormationResourceFunction.build())

    val fileBuilder = FileSpec.builder(packageName, className)
        .addType(typeSpecBuilder.build())

    fileBuilder.addComment("\nDebug: ResourceType")
    fileBuilder.addComment("\nReference: $documentationLink")

    val extensionFunctionBuilder = createStackExtensionFunction(requiredParamSpecs, packageName, className)
    fileBuilder.addFunction(extensionFunctionBuilder)

    fileBuilder
        .build()
        .writeTo(Path.of(GEN_DIR))
}

private fun createToCloudFormationResourceFunction(properties: MutableList<PropertySpec>): FunSpec.Builder {
    val mm = ClassName("kotlin.collections", "mutableMapOf")
    val mapOfProperties =
        mm.parameterizedBy(String::class.asClassName(), Any::class.asClassName().copy(nullable = true))
    val toCloudFormationResourceFunction = FunSpec.builder("toCloudFormationResource")
        .addParameter("logicalId", String::class.asTypeName())
        .addStatement("val props = %T()", mapOfProperties)
    properties.filter { it.name != "cloudFormationType" }.forEach {
        toCloudFormationResourceFunction.addStatement("props[%S] = this.%L", it.name.capitalize(), it.name)
    }
    toCloudFormationResourceFunction
        .addStatement("return CloudFormationResource(logicalId, this.cloudFormationType, props)")
        .returns(CloudFormationResource::class)
    return toCloudFormationResourceFunction
}

private fun createStackExtensionFunction(
    requiredParamSpecs: MutableList<ParameterSpec> = mutableListOf(),
    packageName: String,
    className: String
): FunSpec {

    val fullyQualifiedClassName = packageName.plus(".").plus(className)
    val extensionFunctionAlias = packageName.substring(packageName.lastIndexOf(".") + 1).plus(className)

    val extensionFunctionBuilder = FunSpec.builder(extensionFunctionAlias)
        .receiver(io.futz.aws.model.Stack::class)
        .returns(CloudFormationResource::class)
        .addParameter("logicalId", String::class)

    requiredParamSpecs.forEach {
        extensionFunctionBuilder.addParameter(it)
    }

    val lambdaTypeName = LambdaTypeName.get(ClassName(packageName, className), returnType = Unit::class.asTypeName())
        .copy(nullable = true)
    val lambdaParam = ParameterSpec.builder("init", lambdaTypeName)
        .defaultValue(DEFAULT_NULL)
        .build()
    extensionFunctionBuilder.addParameter(lambdaParam)

    val joiner = StringJoiner(",")
    requiredParamSpecs.forEach { joiner.add("${it.name} = ${it.name}") }
    extensionFunctionBuilder.addStatement("val obj = $fullyQualifiedClassName($joiner)")
        .addStatement("init?.invoke(obj)")
        .addStatement("val r = obj.toCloudFormationResource(logicalId)")
        .addStatement("this.resources[logicalId] = r")
        .addStatement("return r")
    return extensionFunctionBuilder.build()
}


// TODO add comment with a note about being generated
private fun processPropertyTypeEntry(entry: Map.Entry<String, PropertyType>, pMap: MutableMap<String, String>) {

    val parts = KeyParser().parse(entry.key)
    val packageName = parts[0]
    val className = parts[1]

    val primaryCtor = FunSpec.constructorBuilder()
    val properties = mutableListOf<PropertySpec>()

    val props = entry.value.properties.entries
    props.sortedBy { it.key }.forEach {
        val key = it.key
        val name = key.decapitalize()
        val prop = it.value
        val req = prop.required

        val mutable = when (prop.updateType) {
            MUTABLE -> true
            else -> false
        }

        val cn = when {
            req -> getClassName(prop, packageName)
            else -> getClassName(prop, packageName)?.copy(nullable = true)
        }

        val paramSpec = ParameterSpec.builder(name, cn!!)
        when {
            !req ->
                if (prop.type != null) {
                    when {
                        prop.type == LIST -> paramSpec.defaultValue(DEFAULT_EMPTY_LIST)
                        prop.type == MAP -> paramSpec.defaultValue(DEFAULT_EMPTY_MAP)
                        else -> paramSpec.defaultValue(DEFAULT_NULL)
                    }
                } else {
                    paramSpec.defaultValue(DEFAULT_NULL)
                }
        }

        primaryCtor.addParameter(paramSpec.build())
        properties.add(PropertySpec.builder(name, cn).initializer(name).mutable(mutable).build())
    }

    val typeSpecBuilder = TypeSpec.classBuilder(className)
    if (props.isNotEmpty()) {
        typeSpecBuilder
            .addModifiers(KModifier.DATA)
            .addProperties(properties.sortedBy { it.name })
            .primaryConstructor(primaryCtor.build())
    }

    pMap[className] = packageName

    val fileBuilder = FileSpec.builder(packageName, className)
        .addType(typeSpecBuilder.build())

    fileBuilder.addComment("\ndebug: PropertyType")
    if (entry.value.documentation != null) {
        fileBuilder.addComment("\nReference: ${entry.value.documentation!!}")
    }

    fileBuilder
        .build()
        .writeTo(Path.of(GEN_DIR))
}



