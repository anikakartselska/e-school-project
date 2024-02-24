import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "3.2.0"
    java
}
node {
    version.set("16.15.0")
    download.set(true)
}

val buildFrontend = tasks.register<NpmTask>("buildFrontend") {
    this.npmCommand.set(listOf("run"))
    args.set(listOf("build"))
    dependsOn(tasks.npmInstall)
    inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
    inputs.dir("node_modules")
    inputs.dir("public")
    inputs.files("package.json", "vite.config.json", "index.html", "tsconfig.json")
    outputs.dir("${projectDir}/dist")
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
