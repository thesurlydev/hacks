package io.futz.aws.parser

import io.futz.aws.model.CloudFormationPropertyDocumentation
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

class DocumentationParser {

    fun parse(specVersion: String, url: String, cache: Boolean = true): Map<String, CloudFormationPropertyDocumentation> {
        println("parsing: $url")

        val fileName = url.substring(url.lastIndexOf("/") + 1)
        val cacheDir = Path.of(".doc-cache", specVersion)
        if (!cacheDir.toFile().exists()) {
            Files.createDirectories(cacheDir)
        }
        val path = cacheDir.resolve(Path.of(fileName))

        val jSoupDoc = if (cache) {
            val jSoupDocFromFile = try {
                Jsoup.parse(path.toFile(), Charsets.UTF_8.displayName())
            } catch (fne: FileNotFoundException) {
                System.out.println("File not found in cache; adding it: ${path.toAbsolutePath()}")
                null
            }
            if (jSoupDocFromFile == null) {
                val response = Jsoup.connect(url).execute()
                val doc = response.parse()
                Files.write(path, doc.outerHtml().toByteArray())
                doc
            } else {
                println("Found in cache!")
                jSoupDocFromFile
            }
        } else {
            val response = Jsoup.connect(url).execute()
            response.parse()
        }



        val docs = mutableMapOf<String, CloudFormationPropertyDocumentation>()

        val top: Element = if (jSoupDoc.select("#main-col-body > h2:matches(Properties) + div.variablelist").size > 0) {
            jSoupDoc.select("#main-col-body > h2:matches(Properties) + div.variablelist").first()
        } else if (jSoupDoc.select("#main-col-body").isNotEmpty()
            && jSoupDoc.select("#main-col-body > h2:matches(Properties)").isNotEmpty()
            && jSoupDoc.select("#main-col-body > h2:matches(Properties) ~ div.variablelist").isNotEmpty()
        ) {
            jSoupDoc.select("#main-col-body > h2:matches(Properties) ~ div.variablelist").first()
        } else {
            System.err.println("Properties not found for url: $url")
            return emptyMap()
        }


        val p = top.select("dl > dt + dd")
        val pSize = top.select("dl > dt > span.term > code").size // number of properties

        for (j in 0 until pSize) {

            val name = top.select("dl > dt > span.term > code")[j].text()

            var desc: String? = null
            var req: String? = null
            var type: String? = null
            var updateReq: String? = null
            var pattern: String? = null
            var minimum: Int? = null
            var maximum: Int? = null
            var allowedValues: Set<String>? = emptySet()

            val prop = p[j]
            val elements = prop.select("p")
            elements.forEach { element ->
                val unk = element.text()
                when {
                    unk.startsWith("Required:") -> req = unk.substring(unk.indexOf(":") + 1).trim()
                    unk.startsWith("Allowed Values:") -> allowedValues =
                        unk.substring(unk.indexOf(":") + 1).split("|").map { it.trim() }.toSet()
                    unk.startsWith("Maximum:") -> maximum = unk.substring(unk.indexOf(":") + 1).trim().toIntOrNull()
                    unk.startsWith("Minimum:") -> minimum = unk.substring(unk.indexOf(":") + 1).trim().toIntOrNull()
                    unk.startsWith("Pattern:") -> pattern = unk.substring(unk.indexOf(":") + 1).trim()
                    unk.startsWith("Update requires:") -> updateReq = unk.substring(unk.indexOf(":") + 1).trim()
                    unk.startsWith("Type:") -> type = unk.substring(unk.indexOf(":") + 1).trim()
                    else -> desc = unk
                }
                val cfProp = CloudFormationPropertyDocumentation(
                    name,
                    desc,
                    req,
                    type,
                    pattern,
                    minimum,
                    maximum,
                    allowedValues,
                    updateReq
                )
                docs[name.toLowerCase()] = cfProp
            }
        }
        return docs
    }
}

// TODO elaborate on complex types
