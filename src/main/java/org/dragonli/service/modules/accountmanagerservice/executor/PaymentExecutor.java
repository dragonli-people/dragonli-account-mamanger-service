package org.dragonli.service.modules.accountmanagerservice.executor;

import com.alibaba.dubbo.config.annotation.Reference;
import org.dragonli.service.general.interfaces.general.OtherService;
import org.dragonli.service.modules.account.interfaces.AccountChangeService;
import org.dragonli.service.modules.accountmanagerservice.dto.AccountDto;
import org.dragonli.service.modules.accountmanagerservice.dto.FundFlowDto;
import org.dragonli.service.modules.accountservice.entity.enums.BusinessFlowType;
import org.dragonli.service.modules.accountservice.entity.enums.PaymentStatus;
import org.dragonli.service.modules.accountservice.entity.models.AccountEntity;
import org.dragonli.service.modules.accountservice.entity.models.AssetEntity;
import org.dragonli.service.modules.accountservice.entity.models.BusinessEntity;
import org.dragonli.service.modules.accountservice.entity.models.PaymentEntity;
import org.dragonli.service.modules.accountservice.repository.*;
import org.dragonli.service.modules.user.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PaymentExecutor {
	
	@Reference
	AccountChangeService accountService;
	
	@Reference
	OtherService otherService;
	
	@Reference
	UserService userService;

	@Autowired
	AssetRepository assetRepository;

	@Autowired
	AccountsRepository accountsRepository;
	
	@Autowired
	FundFlowEvidenceRepository fundFlowEvidenceRepository;
	
	@Autowired
	PaymentRepository paymentRepository;
	
	@Autowired
	BusinessRepository businessRepository;

	@Autowired
	WithdrawalRepository withdrawalRepository;

	@Autowired
	AccountCreateExecutor accountCreateExecutor;

	@Autowired
	BusinessExecutor businessExecutor;

	@Value("${ADMIN_USER_NAME}")
	private String adminUserName;
	
	final Logger logger = LoggerFactory.getLogger(getClass());

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public PaymentEntity payment(Long userId,String reflexId,String target
			,BigDecimal amount,String currency,String orderId,String remark) throws Exception {
		Map<String,Object> admin = userService.findUser(adminUserName);
		Long adminUserId = (Long)admin.get("id");

		return payment(new AccountDto(userId,reflexId,currency),new AccountDto(adminUserId,target,currency)
				, userId, target , amount, currency, orderId, remark);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public PaymentEntity payment(AccountDto accountFrom,AccountDto accountTo,Long userId,String target
		,BigDecimal amount,String currency,String orderId,String remark) throws Exception {


		AssetEntity asset = assetRepository.findByCurrency(currency);
		AccountEntity fromAccount = accountsRepository.get(
				accountCreateExecutor.createChildAccount(accountFrom.getUserId(), accountFrom.getReflexId(),  asset) );
		AccountEntity toAccount = accountsRepository.get(
				accountCreateExecutor.createChildAccount(accountTo.getUserId(), accountTo.getReflexId(),  asset) );

		return payment(fromAccount.getId(),toAccount.getId(),userId,target,amount,currency,orderId,remark);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public PaymentEntity payment(long accountIdFrom,long accountIdTo,Long userId,String target
			,BigDecimal amount,String currency,String orderId,String remark) throws Exception {

//		final String type = BusinessFlowType.payAccount.name();
//		int step = 2;

		PaymentEntity paymentEntity = paymentRepository.findByOrderId(orderId);
		if (null != paymentEntity) return paymentEntity;

		paymentEntity = new PaymentEntity();

		paymentEntity.setOrderId(orderId);
		paymentEntity.setCurrency(currency);
		//后面有重复，待调整
		paymentEntity.setFromAccountId(accountIdFrom);
		paymentEntity.setToAccountId(accountIdTo);
		paymentEntity.setStatus(PaymentStatus.INIT);
		paymentEntity.setOutTime(3000L);
		paymentEntity.setAmount(amount);
		paymentEntity.setCurrency(currency);
		paymentEntity.setRemark(remark);
		paymentEntity.setUserId(userId);
		paymentEntity.setTarget(target);

		return paymentEntity;
	}

	@Transactional
	public BusinessEntity createBusinessForPayment(Long id) throws Exception{
		PaymentEntity paymentEntity = paymentRepository.get(id);
		List<FundFlowDto> funds = new ArrayList<>(2);
		//扣费一定要先作
		if(paymentEntity.getAmount().compareTo(BigDecimal.ZERO)>0){
			funds.add( new FundFlowDto( paymentEntity.getToAccountId(),paymentEntity.getAmount().negate()));
			funds.add( new FundFlowDto( paymentEntity.getFromAccountId(),paymentEntity.getAmount()));
		}
		else{
			funds.add( new FundFlowDto( paymentEntity.getFromAccountId(),paymentEntity.getAmount()));
			funds.add( new FundFlowDto( paymentEntity.getToAccountId(),paymentEntity.getAmount().negate()));
		}

		return businessExecutor.createBusiness(paymentEntity.getOrderId(),BusinessFlowType.payAccount,paymentEntity.getId(),funds);
	}





}
