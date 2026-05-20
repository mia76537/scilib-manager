package com.scimanager.core.controller.user;

import com.scimanager.core.entity.User;
import com.scimanager.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * <p>提供系统用户的增删改查接口，所有操作均需管理员权限。</p>
 * 用户信息通过 JWT 拦截器注入的 {@code userId} 和 {@code role} 进行鉴权。
 *
 * <p><b>角色说明：</b></p>
 * <ul>
 *   <li>ADMIN — 管理员，可管理所有用户</li>
 *   <li>MENTOR — 导师，可查看名下学生</li>
 *   <li>STUDENT — 学生，仅可查看自己</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    /**
     * 【GET /api/users/me】获取当前登录用户的个人信息
     *
     * @param currentUserId 从 JWT Token 拦截器注入的当前用户 ID
     * @return 当前用户的完整实体（密码已标注 @JsonProperty(WRITE_ONLY)，不会返回给前端）
     */
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@RequestAttribute("userId") String currentUserId) {
        User user = userService.getUserProfile(currentUserId);
        return ResponseEntity.ok(user);
    }

    /**
     * 【POST /api/users】创建新用户（管理员）
     *
     * <p>创建新用户前会校验：</p>
     * <ol>
     *   <li>操作者是否为管理员</li>
     *   <li>用户 ID 是否已被占用</li>
     * </ol>
     *
     * @param user       待创建的用户对象（userId, userName, password, role, mentorId）
     * @param operatorId 从 JWT Token 拦截器注入的当前操作者 ID
     * @return 持久化后的用户实体
     */
    @PostMapping
    public ResponseEntity<User> addUser(
            @RequestBody User user, 
            @RequestAttribute("userId") String operatorId) {
        User savedUser = userService.saveUser(user, operatorId);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * 【PUT /api/users/{id}】更新指定用户信息（管理员）
     *
     * <p>可更新字段：userName、role、mentorId、password（非空时更新）。</p>
     *
     * @param id         目标用户 ID
     * @param user       包含更新信息的实体
     * @param operatorId 当前操作者 ID（需为管理员）
     * @return 操作成功提示
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable String id, 
            @RequestBody User user, 
            @RequestAttribute("userId") String operatorId) {
        userService.updateUser(id, user, operatorId);
        return ResponseEntity.ok("用户更新成功");
    }

    /**
     * 【DELETE /api/users/{id}】删除指定用户（管理员）
     *
     * <p>级联删除该用户的所有关联数据（论文、查收查引请求）。</p>
     *
     * @param id         目标用户 ID
     * @param operatorId 当前操作者 ID（需为管理员）
     * @return 操作成功提示
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable String id, 
            @RequestAttribute("userId") String operatorId) {
        userService.deleteUser(id, operatorId);
        return ResponseEntity.ok("用户删除成功");
    }

    /**
     * 【GET /api/users】获取用户列表
     *
     * <p>根据角色返回不同范围的数据：</p>
     * <ul>
     *   <li>管理员 — 所有用户</li>
     *   <li>导师 — 名下所有学生</li>
     *   <li>学生 — 抛出权限异常</li>
     * </ul>
     *
     * @param operatorId 当前操作者 ID
     * @return 用户实体列表
     */
    @GetMapping
    public ResponseEntity<List<User>> listUsers(
            @RequestAttribute("userId") String operatorId) {
        List<User> users = userService.findAllUsers(operatorId);
        return ResponseEntity.ok(users);
    }
}