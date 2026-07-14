package com.scimanager.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

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
import com.scimanager.core.security.PasswordEncoder;

/**
 * {@link UserServiceImpl} 的单元测试
 *
 * <p>使用 Mockito 模拟 Repository 层，聚焦业务逻辑与权限校验的正确性。</p>
 *
 * <p><b>测试策略：</b></p>
 * <ul>
 *   <li>每个业务方法均覆盖「正常路径」与「异常路径」</li>
 *   <li>权限校验（checkAdminPermission）通过各公开方法间接验证</li>
 *   <li>使用 AssertJ 流式断言提升可读性</li>
 *   <li>采用 BDDMockito 的 given/then 风格</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    private static final String ADMIN_ID = "admin001";
    private static final String MENTOR_ID = "mentor001";
    private static final String STUDENT_ID = "student001";
    private static final String NON_EXISTENT_ID = "nonexistent";
    private static final String TARGET_USER_ID = "target001";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User adminUser;
    private User mentorUser;
    private User studentUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        adminUser = createUser(ADMIN_ID, "管理员", "ADMIN", null);
        mentorUser = createUser(MENTOR_ID, "导师张三", "MENTOR", null);
        studentUser = createUser(STUDENT_ID, "学生李四", "STUDENT", MENTOR_ID);
        targetUser = createUser(TARGET_USER_ID, "目标用户", "STUDENT", MENTOR_ID);
    }

    // ==================== 辅助方法 ====================

    private static User createUser(String userId, String userName, String role, String mentorId) {
        User user = new User();
        user.setUserId(userId);
        user.setUserName(userName);
        user.setPassword("password123");
        user.setRole(role);
        user.setMentorId(mentorId);
        return user;
    }

    // ==================== getUserProfile ====================

    @Nested
    @DisplayName("getUserProfile()")
    class GetUserProfile {

        @Test
        @DisplayName("用户存在时返回用户信息")
        void shouldReturnUserWhenExists() {
            // given
            given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(studentUser));

            // when
            User result = userService.getUserProfile(STUDENT_ID);

            // then
            assertThat(result)
                    .isNotNull()
                    .satisfies(user -> {
                        assertThat(user.getUserId()).isEqualTo(STUDENT_ID);
                        assertThat(user.getUserName()).isEqualTo("学生李四");
                        assertThat(user.getRole()).isEqualTo("STUDENT");
                        assertThat(user.getMentorId()).isEqualTo(MENTOR_ID);
                    });
            then(userRepository).should().findById(STUDENT_ID);
        }

        @Test
        @DisplayName("用户不存在时抛出异常")
        void shouldThrowWhenUserNotFound() {
            // given
            given(userRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserProfile(NON_EXISTENT_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("用户不存在");
            then(userRepository).should().findById(NON_EXISTENT_ID);
        }
    }

    // ==================== saveUser ====================

    @Nested
    @DisplayName("saveUser()")
    class SaveUser {

        @Test
        @DisplayName("管理员创建新用户成功")
        void shouldSaveUserWhenAdmin() {
            // given
            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.existsById(TARGET_USER_ID)).willReturn(false);
            given(userRepository.save(targetUser)).willReturn(targetUser);

            // when
            User result = userService.saveUser(targetUser, ADMIN_ID);

            // then
            assertThat(result).isEqualTo(targetUser);
            then(userRepository).should().existsById(TARGET_USER_ID);
            then(userRepository).should().save(targetUser);
        }

        @Test
        @DisplayName("非管理员创建用户时抛出权限异常")
        void shouldThrowWhenNotAdmin() {
            // given
            given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(studentUser));

            // when & then
            assertThatThrownBy(() -> userService.saveUser(targetUser, STUDENT_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("权限不足：仅限管理员操作");
            then(userRepository).should(never()).existsById(anyString());
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("用户 ID 已存在时抛出异常")
        void shouldThrowWhenUserIdExists() {
            // given
            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.existsById(TARGET_USER_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.saveUser(targetUser, ADMIN_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("用户ID已存在");
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("操作者不存在时抛出异常")
        void shouldThrowWhenOperatorNotFound() {
            // given
            given(userRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.saveUser(targetUser, NON_EXISTENT_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("操作员不存在");
            then(userRepository).should(never()).existsById(anyString());
            then(userRepository).should(never()).save(any());
        }
    }

    // ==================== updateUser ====================

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("管理员成功更新用户信息（不含密码）")
        void shouldUpdateUserWithoutPassword() {
            // given
            User details = createUser(TARGET_USER_ID, "新名字", "MENTOR", null);
            details.setPassword(null);

            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.findById(TARGET_USER_ID)).willReturn(Optional.of(targetUser));

            // when
            userService.updateUser(TARGET_USER_ID, details, ADMIN_ID);

            // then
            assertThat(targetUser.getUserName()).isEqualTo("新名字");
            assertThat(targetUser.getRole()).isEqualTo("MENTOR");
            assertThat(targetUser.getMentorId()).isNull();
            // 密码为空时不应更新
            assertThat(targetUser.getPassword()).isEqualTo("password123");
            then(userRepository).should().save(targetUser);
        }

        @Test
        @DisplayName("管理员成功更新用户信息（含密码）")
        void shouldUpdateUserWithPassword() {
            // given
            User details = createUser(TARGET_USER_ID, "新名字", "MENTOR", null);
            details.setPassword("newPassword456");

            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.findById(TARGET_USER_ID)).willReturn(Optional.of(targetUser));

            // when
            userService.updateUser(TARGET_USER_ID, details, ADMIN_ID);

            // then
            assertThat(targetUser.getPassword()).isEqualTo("newPassword456");
            then(userRepository).should().save(targetUser);
        }

        @Test
        @DisplayName("非管理员更新用户时抛出权限异常")
        void shouldThrowWhenNotAdmin() {
            // given
            given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(studentUser));

            // when & then
            assertThatThrownBy(() -> userService.updateUser(TARGET_USER_ID, targetUser, STUDENT_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("权限不足：仅限管理员操作");
            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("目标用户不存在时抛出异常")
        void shouldThrowWhenTargetUserNotFound() {
            // given
            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateUser(NON_EXISTENT_ID, targetUser, ADMIN_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("目标用户不存在");
            then(userRepository).should(never()).save(any());
        }
    }

    // ==================== deleteUser ====================

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("管理员成功删除用户")
        void shouldDeleteUserWhenAdmin() {
            // given
            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.existsById(TARGET_USER_ID)).willReturn(true);

            // when
            userService.deleteUser(TARGET_USER_ID, ADMIN_ID);

            // then
            then(userRepository).should().deleteById(TARGET_USER_ID);
        }

        @Test
        @DisplayName("非管理员删除用户时抛出权限异常")
        void shouldThrowWhenNotAdmin() {
            // given
            given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentorUser));

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(TARGET_USER_ID, MENTOR_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("权限不足：仅限管理员操作");
            then(userRepository).should(never()).deleteById(anyString());
        }

        @Test
        @DisplayName("目标用户不存在时抛出异常")
        void shouldThrowWhenTargetUserNotFound() {
            // given
            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.existsById(NON_EXISTENT_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(NON_EXISTENT_ID, ADMIN_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("用户不存在");
            then(userRepository).should(never()).deleteById(anyString());
        }
    }

    // ==================== findAllUsers ====================

    @Nested
    @DisplayName("findAllUsers()")
    class FindAllUsers {

        @Test
        @DisplayName("管理员查看所有用户")
        void shouldReturnAllUsersWhenAdmin() {
            // given
            given(userRepository.findById(ADMIN_ID)).willReturn(Optional.of(adminUser));
            given(userRepository.findAll()).willReturn(List.of(adminUser, mentorUser, studentUser));

            // when
            List<User> result = userService.findAllUsers(ADMIN_ID);

            // then
            assertThat(result)
                    .isNotNull()
                    .hasSize(3)
                    .extracting(User::getUserId)
                    .containsExactlyInAnyOrder(ADMIN_ID, MENTOR_ID, STUDENT_ID);
            then(userRepository).should().findAll();
            then(userRepository).should(never()).findByMentorId(anyString());
        }

        @Test
        @DisplayName("导师查看名下学生列表")
        void shouldReturnStudentsWhenMentor() {
            // given
            given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentorUser));
            given(userRepository.findByMentorId(MENTOR_ID))
                    .willReturn(List.of(studentUser));

            // when
            List<User> result = userService.findAllUsers(MENTOR_ID);

            // then
            assertThat(result)
                    .isNotNull()
                    .hasSize(1)
                    .first()
                    .satisfies(user -> {
                        assertThat(user.getUserId()).isEqualTo(STUDENT_ID);
                        assertThat(user.getMentorId()).isEqualTo(MENTOR_ID);
                    });
            then(userRepository).should(never()).findAll();
            then(userRepository).should().findByMentorId(MENTOR_ID);
        }

        @Test
        @DisplayName("导师名下无学生时返回空列表")
        void shouldReturnEmptyListWhenMentorHasNoStudents() {
            // given
            given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentorUser));
            given(userRepository.findByMentorId(MENTOR_ID)).willReturn(List.of());

            // when
            List<User> result = userService.findAllUsers(MENTOR_ID);

            // then
            assertThat(result).isNotNull().isEmpty();
            then(userRepository).should().findByMentorId(MENTOR_ID);
        }

        @Test
        @DisplayName("学生查看用户列表时抛出权限异常")
        void shouldThrowWhenStudent() {
            // given
            given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(studentUser));

            // when & then
            assertThatThrownBy(() -> userService.findAllUsers(STUDENT_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("权限不足：仅限管理员或导师查看用户列表");
            then(userRepository).should(never()).findAll();
            then(userRepository).should(never()).findByMentorId(anyString());
        }

        @Test
        @DisplayName("操作者不存在时抛出异常")
        void shouldThrowWhenOperatorNotFound() {
            // given
            given(userRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.findAllUsers(NON_EXISTENT_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("操作员不存在");
            then(userRepository).should(never()).findAll();
            then(userRepository).should(never()).findByMentorId(anyString());
        }
    }
}
