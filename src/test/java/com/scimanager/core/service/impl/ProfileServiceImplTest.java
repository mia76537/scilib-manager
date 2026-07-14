package com.scimanager.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scimanager.core.dto.user.PasswordUpdateDTO;
import com.scimanager.core.dto.user.UserProfileDTO;
import com.scimanager.core.entity.User;
import com.scimanager.core.mapper.UserMapper;
import com.scimanager.core.repository.UserRepository;

/**
 * {@link ProfileServiceImpl} 的单元测试
 *
 * <p>使用 Mockito 模拟 UserRepository 和 UserMapper，聚焦个人资料管理与密码修改逻辑。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileServiceImpl 单元测试")
class ProfileServiceImplTest {

    private static final String USER_ID = "user001";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private User mockUser;
    private UserProfileDTO mockDto;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUserId(USER_ID);
        mockUser.setUserName("测试用户");
        mockUser.setPassword("password123");
        mockUser.setRole("STUDENT");
        mockUser.setMentorId("mentor001");

        mockDto = new UserProfileDTO();
        mockDto.setUserId(USER_ID);
        mockDto.setUserName("测试用户");
        mockDto.setRole("STUDENT");
        mockDto.setMentorId("mentor001");
    }

    // ==================== getProfile ====================

    @Nested
    @DisplayName("getProfile()")
    class GetProfile {

        @Test
        @DisplayName("用户存在时返回 UserProfileDTO")
        void shouldReturnDtoWhenUserExists() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));
            given(userMapper.toProfileDto(mockUser)).willReturn(mockDto);

            // when
            UserProfileDTO result = profileService.getProfile(USER_ID);

            // then
            assertThat(result)
                    .isNotNull()
                    .satisfies(dto -> {
                        assertThat(dto.getUserId()).isEqualTo(USER_ID);
                        assertThat(dto.getUserName()).isEqualTo("测试用户");
                        assertThat(dto.getRole()).isEqualTo("STUDENT");
                        assertThat(dto.getMentorId()).isEqualTo("mentor001");
                    });
            then(userRepository).should().findById(USER_ID);
            then(userMapper).should().toProfileDto(mockUser);
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            // given
            String nonExistentId = "nonexistent";
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.getProfile(nonExistentId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("该用户不存在");
            then(userRepository).should().findById(nonExistentId);
            then(userMapper).shouldHaveNoInteractions();
        }
    }

    // ==================== updateProfile ====================

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfile {

        @Test
        @DisplayName("正常更新用户信息")
        void shouldUpdateProfileSuccessfully() {
            // given
            UserProfileDTO updateDto = new UserProfileDTO();
            updateDto.setUserName("新名字");

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));

            // when
            profileService.updateProfile(USER_ID, updateDto);

            // then
            then(userMapper).should().updateEntityFromDto(updateDto, mockUser);
            then(userRepository).should().save(mockUser);
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            // given
            String nonExistentId = "nonexistent";
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.updateProfile(nonExistentId, mockDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("用户身份验证失败");
            then(userMapper).shouldHaveNoInteractions();
            then(userRepository).should(never()).save(any());
        }
    }

    // ==================== changePassword ====================

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("原密码正确时成功更新密码")
        void shouldChangePasswordWhenOldPasswordMatches() {
            // given
            PasswordUpdateDTO pwdDto = new PasswordUpdateDTO();
            pwdDto.setOldPassword("password123");
            pwdDto.setNewPassword("newPassword456");

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));

            // when
            profileService.changePassword(USER_ID, pwdDto);

            // then
            assertThat(mockUser.getPassword()).isEqualTo("newPassword456");
            then(userRepository).should().save(mockUser);
        }

        @Test
        @DisplayName("原密码错误时抛出异常")
        void shouldThrowWhenOldPasswordWrong() {
            // given
            PasswordUpdateDTO pwdDto = new PasswordUpdateDTO();
            pwdDto.setOldPassword("wrongOldPassword");
            pwdDto.setNewPassword("newPassword456");

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(mockUser));

            // when & then
            assertThatThrownBy(() -> profileService.changePassword(USER_ID, pwdDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("原密码校验失败，请重试");
            // 密码不应被修改
            assertThat(mockUser.getPassword()).isEqualTo("password123");
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            // given
            String nonExistentId = "nonexistent";
            PasswordUpdateDTO pwdDto = new PasswordUpdateDTO();
            pwdDto.setOldPassword("password123");
            pwdDto.setNewPassword("newPassword456");

            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileService.changePassword(nonExistentId, pwdDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("用户不存在");
            then(userRepository).should(never()).save(any());
        }
    }
}
