package org.dragonli.service.modules.accountmanagerservice.executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class WithdrawalExecutor {


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Map<String,Object> accountWithdrawal(
            String reflexId, BigDecimal amount,String currency,String orderId,
            String address,String addressExtend
    ) throws Exception {
        return null;
		/*
		String type = BusinessFlowType.childAccountWithdrawal.name() ;

		logger.info("childAccountWithdrawal paras reflexId : "+reflexId+" type : "+type+" currency : "
				+currency+" aomunt : "+amount+" orderId : "+orderId+" applicationId : "+applicationId);

		Map<String,Object> result = new HashMap<>();
		if(null == reflexId || null == type || null == currency || null == amount || null == orderId
				|| amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal("10000000"))>0) {
			result.put("errorCode","PARAS_IS_NULL");
			result.put("result",false);
			return result;
		}
		Withdrawal withdrawal = withdrawalRepository.findFirstByOrderId(orderId);
		if(null != withdrawal) {//&& true即且无权限查看，即非操作发起方。暂时不处理
			result.put("status",withdrawal.getStatus().name());
			result.put("result",false);
			return result;
		}
		Application application = applicationRepository.getOne(applicationId);
		if(null == application) {
			result.put("errorCode","APPLICATION_NOT_FOUND");
			result.put("result",false);
			return result;
		}
		Asset asset = assetRepository.findByCode(currency);
		UserApplication userApplication = userApplicationRepository
				.findByApplicationIdAndReflexIdAndAssetName(applicationId,reflexId,asset.getCode());
		Enterprise enterprise = enterpriseRepository.getOne(application.getEnterpriseId());
		User enterpriseUser = enterprise == null ? null : userRepository.getOne(enterprise.getUserId());
		User user = userApplication == null ? null : userRepository.getOne(userApplication.getUserId());
		Accounts enterAccount = enterpriseUser == null ? null : accountsRepository.getOne(
				accountCreateExecutor.createChildAccount(enterpriseUser,  application, "",  asset) );
		Accounts userAccount = user == null ? null : accountsRepository.getOne(
				accountCreateExecutor.createChildAccount(user,  application, reflexId,  asset) );
		if(null == userApplication || userAccount == null || enterAccount == null
				|| amount.compareTo(userAccount.getBalance()) > 0 ){//已存在的转帐，同步结果
			result.put("errorCode","BALANCE_NOT_ENOUGH");
			result.put("result",false);
			return result;
		}



//		Enterprise enter = enterpriseRepository.getOne(applicationId);

		withdrawal = new Withdrawal();

		withdrawal.setEnterpriseId(enterprise.getId());
		withdrawal.setUserId(enterprise.getUserId());
		withdrawal.setApplicationId(application.getId());
		withdrawal.setWithdrawalUserId(user.getId());
		withdrawal.setEnuserId(reflexId);
		withdrawal.setOrderId(orderId);
		withdrawal.setBackOrderId(UUID.randomUUID().toString());
		withdrawal.setAmount(amount);
		withdrawal.setFees(asset.getFee());
		withdrawal.setFinalAmount(amount.subtract(amount.multiply(withdrawal.getFees())));
		withdrawal.setCurrency(currency);
		withdrawal.setOwnerId(enterprise.getAccountOwnerId());

		withdrawal.setWithdrawalAccountId(enterAccount.getId());
		withdrawal.setWithdrawalChildAccountId(userAccount.getId());
		withdrawal.setWithdrawalBusinessId(0L);//需要运行中设置
		withdrawal.setWithdrawalChildFlowerId(null);//不需要了

		withdrawal.setStatus(WithdrawalStatus.CHILD_CHECHING);
		withdrawal.setCurrency(currency);

		withdrawal.setAddress(address);
		withdrawal.setAddressExtend(addressExtend);
		withdrawal.setVersion(0);

		//todo 提现拦截若干：门槛和每日上限

		if(true)//企业是否设置了不验证子帐户，暂时恒为true
			withdrawal.setStatus(WithdrawalStatus.CHILD_HAD_BE_ACCEPT);

		withdrawal = withdrawalRepository.save(withdrawal); //accountCreateExecutor.addWithdrawal(withdrawal);
		Long withdrawalId = withdrawal.getId();
		logger.info("生成transfer记录  id 1: "+withdrawalId);
		withdrawal = withdrawalRepository.getOne(withdrawalId);
		logger.info("生成transfer记录  id 2: "+withdrawalId);

		logger.info("生成transfer记录  id 3: "+withdrawalId);
		logger.info("生成transfer记录  id 4: "+withdrawal.getId()+"|"+withdrawal.getStatus().name());
		logger.info("生成transfer记录  id 5:" + withdrawalId);

		withdrawalRepository.refresh(withdrawal);

		int step = 3;
		Business buss = new Business();
		buss.setEnterpriseId(applicationId);
		buss.setUserId(reflexId);
		Long busId = accountCreateExecutor.createBusinessAndResultForNext(
				buss,withdrawalId,orderId,type,step,null);
		logger.info("+++++++++++++"+busId);
		withdrawal.setWithdrawalBusinessId(busId);
		logger.info("+++++++++++++"+withdrawal.getId());
		//增加了business即视为已发出支付。发出动作在调用外层。但支付的回调必须见到这个状态才行进行下一步操作
		withdrawal.setStatus(WithdrawalStatus.CHILD_HAD_TRASFER);
		withdrawal = withdrawalRepository.save(withdrawal);

		final Withdrawal withdrawalCopy = withdrawal;
		//withdrawal更新有问题 会重新插入一条
//		Map<String,Object> body = new HashMap<>();
		//可以不传
//		body.put("userAccount", userAccount.getBalance());
//		body.put("enterAccount", enterAccount.getBalance());

//		body.put("tranId", business.getTranId());//已弃用
//		body.put("steps", step);//可以不用
//		body.put("type", flowType);//可以不用
		result.put("busId", busId);
		List<Map<String,Object>> accountIdList = new ArrayList<>(step);
		accountIdList.add( new HashMap<String,Object>(){{
			put("accountId",userAccount.getId());
			put("amount",withdrawalCopy.getAmount().negate());
		}} );
		accountIdList.add( new HashMap<String,Object>(){{
			put("accountId",enterAccount.getId());
			put("amount",withdrawalCopy.getAmount());
		}} );
		accountIdList.add( new HashMap<String,Object>(){{
			put("accountId",enterAccount.getId());
			put("amount",withdrawalCopy.getAmount().negate());
		}} );
		result.put("funds", accountIdList);

		result.put("result",true);
		result.put("status",withdrawal.getStatus().name());
		return result;
		*/
    }

}
