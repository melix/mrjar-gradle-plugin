package me.champeau.mrjar

import groovy.transform.CompileStatic

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

@CompileStatic
class JarFixture implements Closeable {
    private final Path jarPath
    private final FileSystem fileSystem

    JarFixture(File jarFile) {
        jarPath = jarFile.toPath()
        fileSystem = FileSystems.newFileSystem(jarPath, null)
    }

    void isMultiRelease() {
        assert textOf("META-INF/MANIFEST.MF").contains("Multi-Release: true")
    }

    Path hasFile(String path) {
        def element = fileSystem.getPath(path)
        assert Files.isRegularFile(element)
        element
    }

    String textOf(String path, String encoding = "utf-8") {
        Path p = hasFile(path)
        new String(Files.readAllBytes(p), encoding)
    }

    @Override
    void close() throws IOException {
        fileSystem.close()
    }
}
