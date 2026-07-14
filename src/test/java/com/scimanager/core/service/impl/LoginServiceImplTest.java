package com.scimanager.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scimanager.core.entity.User;
import com.scimanager.core.repository.UserRepository;

/**
 * {@link LoginServiceImpl} 的单元测试
 *
 * <p>使用 Mockito 模拟 UserRepository，聚焦登录认证逻辑。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginServiceImpl 单元测试")
class LoginServiceImplTest {

    private static final String USER_ID = "user001";
    private static final String PASSWORD = "password123";
    private static final String WRONG_PASSWORD = "wrongPassword";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginServiceImpl loginService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(USER_ID);
        mockUser.setUserName("测试用户");
        mockUser.setPassword(PASSWORD);
        mockUser.setRole("STUDENT");
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("userId 和 password 正确时返回 User 实体")
        void shouldReturnUserWhenCredentialsCorrect() {
            // given
            given(userRepository.findByUserIdAndPassword(USER_ID, PASSWORD)).willReturn(mockUser);

            // when
            User result = loginService.login(USER_ID, PASSWORD);

            // then
            assertThat(result)
                    .isNotNull()
                    .satisfies(user -> {
                        assertThat(user.getUserId()).isEqualTo(USER_ID);
                        assertThat(user.getUserName()).isEqualTo("测试用户");
                        assertThat(user.getRole()).isEqualTo("STUDENT");
                    });
            then(userRepository).should().findByUserIdAndPassword(USER_ID, PASSWORD);
        }

        @Test
        @DisplayName("password 错误时返回 null")
        void shouldReturnNullWhenPasswordWrong() {
            // given
            given(userRepository.findByUserIdAndPassword(USER_ID, WRONG_PASSWORD)).willReturn(null);

            // when
            User result = loginService.login(USER_ID, WRONG_PASSWORD);

            // then
            assertThat(result).isNull();
            then(userRepository).should().findByUserIdAndPassword(USER_ID, WRONG_PASSWORD);
        }

        @Test
        @DisplayName("userId 不存在时返回 null")
        void shouldReturnNullWhenUserNotFound() {
            // given
            String nonExistentId = "nonexistent";
            given(userRepository.findByUserIdAndPassword(nonExistentId, PASSWORD)).willReturn(null);

            // when
            User result = loginService.login(nonExistentId, PASSWORD);

            // then
            assertThat(result).isNull();
            then(userRepository).should().findByUserIdAndPassword(nonExistentId, PASSWORD);
        }
    }
}
