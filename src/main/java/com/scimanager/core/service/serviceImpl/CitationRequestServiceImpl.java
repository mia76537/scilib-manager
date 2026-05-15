package com.scimanager.core.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scimanager.core.model.CitationItem;
import com.scimanager.core.model.CitationRequest;
import com.scimanager.core.model.CitationRequest.RequestStatus;
import com.scimanager.core.model.CitationResult;
import com.scimanager.core.model.User;
import com.scimanager.core.model.dto.citationrequesdto.CitationResultSubmitDTO;
import com.scimanager.core.model.dto.citationrequesdto.CreateCitationRequestDTO;
import com.scimanager.core.repository.CitationRequestRepository;
import com.scimanager.core.repository.CitationResultRepository;
import com.scimanager.core.repository.UserRepository;
import com.scimanager.core.service.CitationRequestService;
import com.scimanager.core.service.mapper.CitationMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CitationRequestServiceImpl implements CitationRequestService {

	private final CitationRequestRepository requestRepository;
	private final UserRepository userRepository;
	private final CitationMapper citationMapper;
	private final CitationResultRepository resultRepository;

	@PersistenceContext
	private EntityManager entityManager;

	// --- 内部辅助方法：权限检查 ---
	private boolean isAdmin(String userId) {
		User user = userRepository.findByUserId(userId);
		return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
	}

	// 创建请求
	@Override
	@Transactional(rollbackFor = Exception.class)
	public CitationRequest createCitationRequest(CreateCitationRequestDTO dto, String userId) {
		User user = userRepository.findByUserId(userId);
		if (user == null)
			throw new RuntimeException("用户不存在");

		CitationRequest request = citationMapper.toEntity(dto, user, generateSerialNumber(), LocalDateTime.now());

		return requestRepository.save(request);

	}

	// 查看详情
	@Override
	public CitationRequest findBySerialNumber(String serialNumber, String userId) {
		CitationRequest request = requestRepository.findBySerialNumber(serialNumber)
				.orElseThrow(() -> new RuntimeException("请求不存在"));

		// 管理员或请求发起者本人可见
		if (!isAdmin(userId) && !request.getRequester().getUserId().equals(userId)) {
			throw new RuntimeException("无权访问该请求");
		}
		return request;
	}

	// 推进状态
	@Override
	@Transactional
	public void updateRequestStatus(String serialNumber, RequestStatus status, String userId) {
		if (!isAdmin(userId)) {
			throw new RuntimeException("权限不足，仅管理员可修改状态");
		}
		CitationRequest request = requestRepository.findBySerialNumber(serialNumber)
				.orElseThrow(() -> new RuntimeException("请求不存在"));

		request.setStatus(status);
		request.setUpdateTime(LocalDateTime.now());
		requestRepository.save(request);
	}

	// 删除请求
	@Override
	@Transactional
	public void deleteRequest(String serialNumber, String userId) {
		CitationRequest request = requestRepository.findBySerialNumber(serialNumber)
				.orElseThrow(() -> new RuntimeException("请求不存在"));

		// 管理员或请求发起者本人可删除
		if (!isAdmin(userId) && !request.getRequester().getUserId().equals(userId)) {
			throw new RuntimeException("无权删除该请求");
		}
		requestRepository.delete(request);
	}

	// 综合条件查询
	@Override
	public List<CitationRequest> findRequestsByCondition(RequestStatus status, String serialNumber, String requesterId,
			String userId) {
		boolean isAdmin = isAdmin(userId);
		// 权限控制：如果是管理员，通过传入的 requesterId 筛选，否则强制限制为当前用户 ID
		String targetUserId = isAdmin ? requesterId : userId;
		return requestRepository.findWithConditions(status, serialNumber, targetUserId);
	}

	@Override
	@Transactional // 提交结果
	public void submitResults(CitationResultSubmitDTO submitDTO, String userId) {
		// 1. 权限校验
		if (!isAdmin(userId)) {
			throw new RuntimeException("权限不足，仅管理员可提交检索结果");
		}

		// 2. 转换新结果
		List<CitationResult> newResults = citationMapper.toResultEntityList(submitDTO.getResults());

		// 3. 处理覆盖逻辑
		// 收集本次提交涉及的所有 itemId
		List<Long> itemIds = submitDTO.getResults().stream().map(CitationResultSubmitDTO.ItemResultEntry::getItemId)
				.distinct().toList();

		// 【关键步骤】：删除这些 item 之前旧的检索结果，防止重复
		if (!itemIds.isEmpty()) {
			resultRepository.deleteByItemIdIn(itemIds);
			// 注意：你需要在 CitationResultRepository 中定义这个方法
		}

		CitationRequest parentRequest = null;

		// 4. 重新建立关联并保存
		for (int i = 0; i < newResults.size(); i++) {
			Long itemId = submitDTO.getResults().get(i).getItemId();
			CitationItem item = entityManager.find(CitationItem.class, itemId);

			if (item != null) {
				newResults.get(i).setItem(item);
				// 如果 parentRequest 为空，则从第一个合法的 item 中获取
				if (parentRequest == null) {
					// 注意：由于 CitationItem.java 中 request 字段设置了 insertable = false,
					// 请确保此时 item.getRequest() 能拿到值。
					// 如果拿不到，建议通过 submitDTO.getSerialNumber() 去查询 request 对象
					parentRequest = requestRepository.findBySerialNumber(submitDTO.getSerialNumber()).orElse(null);
				}
			}
		}

		// 保存新结果
		resultRepository.saveAll(newResults);

		// 5. 更新主表状态和时间
		if (parentRequest != null) {
			parentRequest.setUpdateTime(LocalDateTime.now());
			// 建议：提交结果后，自动将状态改为“已完成”
			parentRequest.setStatus(CitationRequest.RequestStatus.COMPLETED);
			requestRepository.save(parentRequest);
		}
	}

	private String generateSerialNumber() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
