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
		// 显式校验管理员权限
		if (!isAdmin(userId)) {
			throw new RuntimeException("权限不足，仅管理员可提交检索结果");
		}
		List<CitationResult> results = citationMapper.toResultEntityList(submitDTO.getResults());
		// 这些结果属于同一个 Request，需要拿到这个 Request 引用
		CitationRequest parentRequest = null;

		for (int i = 0; i < results.size(); i++) {
			Long itemId = submitDTO.getResults().get(i).getItemId();
			// 建议直接 find 实体，以便获取关联的 Request
			CitationItem item = entityManager.find(CitationItem.class, itemId);
			results.get(i).setItem(item);

			if (parentRequest == null && item != null) {
				parentRequest = item.getRequest();
			}
		}
		// 更新主表时间
		if (parentRequest != null) {
			parentRequest.setUpdateTime(LocalDateTime.now());
			requestRepository.save(parentRequest);
		}
	}

	private String generateSerialNumber() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
