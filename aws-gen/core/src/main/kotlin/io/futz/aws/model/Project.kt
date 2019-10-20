package io.futz.aws.model

data class Project(
    val projectName: String,
    val accountId: String
) {
    val stacks: MutableMap<String, Stack> = mutableMapOf()
}

fun Project.stack(
    stackName: String,
    region: String,
    init: (Stack.() -> Unit)? = null
): Stack {
    val s = Stack(this, stackName, region)
    init?.invoke(s)
    stacks[stackName] = s
    return s
}

fun project(name: String, account: String, block: Project.() -> Unit): Project {
    return Project(name, account).apply(block)
}