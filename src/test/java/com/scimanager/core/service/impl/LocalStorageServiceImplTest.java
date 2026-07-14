package com.scimanager.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * {@link LocalStorageServiceImpl} 的单元测试
 *
 * <p>使用 JUnit 5 {@code @TempDir} 注入临时目录，避免污染真实文件系统。</p>
 */
@DisplayName("LocalStorageServiceImpl 单元测试")
class LocalStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private LocalStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageServiceImpl();
        // 将 uploadPath 注入为临时目录
        ReflectionTestUtils.setField(storageService, "uploadPath", tempDir.toString());
    }

    // ==================== upload ====================

    @Nested
    @DisplayName("upload()")
    class Upload {

        @Test
        @DisplayName("正常上传文件返回 UUID 文件名")
        void shouldReturnUuidFileName() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.pdf",
                    "application/pdf",
                    "Hello, World!".getBytes());

            // when
            String fileName = storageService.upload(file);

            // then
            assertThat(fileName)
                    .isNotNull()
                    .isNotEmpty()
                    .endsWith(".pdf");

            // 验证文件确实被写入磁盘
            Path storedFile = tempDir.resolve(fileName);
            assertThat(Files.exists(storedFile)).isTrue();
            assertThat(Files.readString(storedFile)).isEqualTo("Hello, World!");
        }

        @Test
        @DisplayName("上传空文件正常处理")
        void shouldHandleEmptyFile() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "empty.txt",
                    "text/plain",
                    new byte[0]);

            // when
            String fileName = storageService.upload(file);

            // then
            assertThat(fileName)
                    .isNotNull()
                    .endsWith(".txt");

            Path storedFile = tempDir.resolve(fileName);
            assertThat(Files.exists(storedFile)).isTrue();
            assertThat(Files.size(storedFile)).isZero();
        }

        @Test
        @DisplayName("上传目录不存在时自动创建目录")
        void shouldAutoCreateUploadDirectory() throws Exception {
            // given
            // 将 uploadPath 设置为临时目录下的不存在的子目录
            String nestedDir = tempDir.resolve("sub").resolve("uploads").toString();
            ReflectionTestUtils.setField(storageService, "uploadPath", nestedDir);

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "auto-dir.txt",
                    "text/plain",
                    "content".getBytes());

            // when
            String fileName = storageService.upload(file);

            // then
            assertThat(fileName).endsWith(".txt");
            Path storedFile = Path.of(nestedDir, fileName);
            assertThat(Files.exists(storedFile)).isTrue();
        }

        @Test
        @DisplayName("无扩展名文件正常处理")
        void shouldHandleFileWithoutExtension() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "noextension",
                    "application/octet-stream",
                    "data".getBytes());

            // when
            String fileName = storageService.upload(file);

            // then
            assertThat(fileName).isNotNull().isNotEmpty();
            Path storedFile = tempDir.resolve(fileName);
            assertThat(Files.exists(storedFile)).isTrue();
            assertThat(Files.readString(storedFile)).isEqualTo("data");
        }
    }

    // ==================== delete ====================

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("删除存在的文件成功")
        void shouldDeleteExistingFile() throws Exception {
            // given
            Path existingFile = tempDir.resolve("to-delete.txt");
            Files.writeString(existingFile, "content");
            assertThat(Files.exists(existingFile)).isTrue();

            // when
            storageService.delete("to-delete.txt");

            // then
            assertThat(Files.exists(existingFile)).isFalse();
        }

        @Test
        @DisplayName("删除不存在的文件静默忽略")
        void shouldSilentlyIgnoreNonExistentFile() {
            // when & then — 不抛异常
            storageService.delete("nonexistent-file.txt");
        }
    }

    // ==================== getAbsolutePath ====================

    @Nested
    @DisplayName("getAbsolutePath()")
    class GetAbsolutePath {

        @Test
        @DisplayName("返回文件的绝对路径")
        void shouldReturnAbsolutePath() {
            // given
            String fileName = "some-file.pdf";
            Path expected = tempDir.resolve(fileName).toAbsolutePath();

            // when
            String result = storageService.getAbsolutePath(fileName);

            // then
            assertThat(result).isEqualTo(expected.toString());
        }
    }

    // ==================== loadAsResource ====================

    @Nested
    @DisplayName("loadAsResource()")
    class LoadAsResource {

        @Test
        @DisplayName("存在的文件返回可读的 Resource")
        void shouldReturnReadableResource() throws Exception {
            // given
            String fileName = "download-me.txt";
            Path filePath = tempDir.resolve(fileName);
            Files.writeString(filePath, "downloadable content");

            // when
            Resource resource = storageService.loadAsResource(fileName);

            // then
            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
            assertThat(resource.getContentAsString(/* charset= */ null))
                    .asString()
                    .isEqualTo("downloadable content");
        }

        @Test
        @DisplayName("不存在的文件抛出异常")
        void shouldThrowWhenFileNotFound() {
            // when & then
            assertThatThrownBy(() -> storageService.loadAsResource("ghost-file.txt"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("无法读取文件");
        }
    }
}
