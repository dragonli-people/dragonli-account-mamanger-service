package org.dragonli.service.modules.accountmanagerservice.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import org.dragonli.service.general.interfaces.general.OtherService;
import org.dragonli.service.modules.account.interfaces.AccountChangeService;
import org.dragonli.service.modules.account.interfaces.AccountManagerService;
import org.dragonli.service.modules.accountmanagerservice.executor.DepositExecutor;
import org.dragonli.service.modules.accountmanagerservice.executor.PaymentExecutor;
import org.dragonli.service.modules.accountmanagerservice.executor.WithdrawalExecutor;
import org.dragonli.service.modules.accountservice.constants.AccountConstants;
import org.dragonli.service.modules.accountservice.entity.models.PaymentEntity;
import org.dragonli.service.modules.accountservice.repository.*;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service(interfaceClass=AccountManagerService.class, register = true, timeout = 150000000, retries = -1, delay = -1)
public class AccountManagerServiceImpl {  //implements AccountManagerService {
	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Reference
	AccountChangeService accountService;
	
	@Reference
    OtherService otherService;
	
	
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
	DepositRepository depositRepository;

	@Autowired
	WithdrawalRepository withdrawalRepository;

	@Autowired
	WithdrawalExecutor withdrawalExecutor;
	
	@Autowired
	AccountAdjustmentRepository accountAdjustmentRepository;
	
	@Autowired
	DepositExecutor DepositExecutor;

    @Autowired
    AccountAssetsRecordRepository accountAssetsRecordRepository;

    @Autowired
	PaymentExecutor paymentExecutor;

    @Autowired
    @Qualifier(AccountConstants.ACCOUNT_REDIS)
    RedissonClient accountRedisson;


	@Value("${spring.dubbo-jackpot-service.info.redis.chainxServerPauseSignal}")
	String serverPauseSignalRedisKey;

	@Value("${spring.dubbo-jackpot-service.info.redis.chainxServerPauseSignalInfo}")
	String serverPauseSignalRedisInfoKey;

	private final String CHAINX_SERVICE_PAUSING = "CHAINX_SERVICE_PAUSING";
	private final String CHAINX_SERVICE_PAUSING_INFO = "CHAINX_SERVICE_PAUSING_INFO";


	public Map<String,Object> withdrawal(Map<String,Object> jsonParams) throws Exception {
		// TODO Auto-generated method stub
//		Object serverSignal = accountRedisson.getBucket(serverPauseSignalRedisKey).get();
//		serverSignal = (null == serverSignal || "".equals(serverSignal.toString().trim()))
//				? 0 : Integer.parseInt(serverSignal.toString().trim());
//		if(!serverSignal.equals(0))return null;
		return null;
	}

//	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String payment(Long userId,String reflexId,String target
			,BigDecimal amount,String currency,String orderId,String remark,Boolean readOnly) throws Exception {
		//todo
//		Object serverSignalSource = accountRedisson.getBucket(serverPauseSignalRedisKey).get();
//		final Object serverSignalInfo = accountRedisson.getBucket(serverPauseSignalRedisInfoKey).get();
//		final int serverSignal = (null == serverSignalSource || "".equals(serverSignalSource.toString().trim()))
//				? 0 : Integer.parseInt(serverSignalSource.toString().trim());
//		if(serverSignal != 0)return new HashMap<String,Object>()
//		{{
//			put(CHAINX_SERVICE_PAUSING, serverSignal);
//			put(CHAINX_SERVICE_PAUSING_INFO, serverSignalInfo != null ?serverSignalInfo.toString():"");
//		}};

	    if( readOnly != null && readOnly )
        {
            PaymentEntity paymentEntity = paymentRepository.findByOrderId(orderId);
            return paymentEntity == null ? null : paymentEntity.getStatus().name();
//            return transfer == null ? null : transfer.getStatus().name();
        }
		PaymentEntity paymentEntity = paymentExecutor.payment(userId,reflexId,target,amount,currency,orderId,remark);
		paymentEntity = paymentRepository.get(paymentEntity.getId());
		paymentExecutor.createBusinessForPayment(paymentEntity.getId());
		return paymentEntity.getStatus().name();

//        Map<String,Object> body = chainxServicePayment.payment(username,applicationId,reflexId,amount,currency,orderId,tokenUrl,remark);
//		if((boolean)body.get("result")) {
//			switch(body.get("type").toString()) {
//			case "recharge":
//				recharge(body);
//				break;
//			case "withdrawal":
//				withdrawal(body);
//				break;
//			case "payAccount":
//				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^"+body.toString());
//				payAccount(body);
//				break;
//			}
//		}
//		if((boolean)body.get("result")) payAccount(body);
//		return new HashMap<String,Object>()
//		{{
//			put("status",body.toString());
//			put(CHAINX_SERVICE_PAUSING, serverSignal);
//			put(CHAINX_SERVICE_PAUSING_INFO, serverSignalInfo != null ?serverSignalInfo.toString():"");
//		}};
//		return body.toString();
	}



	@Transactional
	public Map<String, Object> accountWithdrawal(
			Long applicationId, String userId, String amountStr, String currency, String orderId,
			String address,String addressExtend
	) throws Exception {

		/*
		Object serverSignalSource = accountRedisson.getBucket(serverPauseSignalRedisKey).get();
		final Object serverSignalInfo = accountRedisson.getBucket(serverPauseSignalRedisInfoKey).get();
		final int serverSignal = (null == serverSignalSource || "".equals(serverSignalSource.toString().trim()))
				? 0 : Integer.parseInt(serverSignalSource.toString().trim());
		if(serverSignal!=0)return new HashMap<String,Object>()
		{{
			put(CHAINX_SERVICE_PAUSING, serverSignal);
			put(CHAINX_SERVICE_PAUSING_INFO, serverSignalInfo != null ?serverSignalInfo.toString():"");
		}};

		if( applicationId == null || userId == null || amountStr == null || currency == null
				|| orderId == null || address == null || addressExtend == null || "".equals(amountStr.trim())
				|| "".equals(address.trim()) ) return null;

		BigDecimal amount = new BigDecimal(amountStr);
		Map<String,Object> jsonParams = chainxServicePayment.childAccountWithdrawal(
				applicationId,userId,amount,currency,orderId,
				address,addressExtend
		);
		String res = chainxServiceBefore.payAccount(jsonParams);
		JSONObject json = accountService.addChangeRecord("", res);
		json.put(CHAINX_SERVICE_PAUSING,serverSignal);
		json.put(CHAINX_SERVICE_PAUSING_INFO, serverSignalInfo != null ?serverSignalInfo.toString():"");
		return json;
		*/
		return null;
	}


//	@Override
	@Transactional
	public Map<String, Object> userAccountWithdrawal() throws Exception {
		return null;
	}

	@Deprecated
//	@Override
	@Transactional
	public Map<String, Object> getUserAccount(Map<String, Object> jsonParams) throws Exception {
		// TODO Auto-generated method stub
		/*
		Long enterpriseId = Long.parseLong(jsonParams.get("enterpriseId").toString());
		String userId = jsonParams.get("userId").toString();
		String currency = jsonParams.get("currency").toString();
		Map<String,Object> result = new HashMap<>();
		Enterprise enter = enterpriseRepository.getOne(enterpriseId);
		if(null == enter) {
			result.put("errorCode","ENTERPRISE_NOT_FOUND");
			result.put("result",false);
			return result;
		}
		Asset asset = assetRepository.findByCode(currency);
		if(null == asset) {
			result.put("errorCode","ASSET_NOT_EXIST");
			result.put("result",false);
			return result;
		}

		Accounts userAccount = accountsRepository.findByUserIdAndReflexIdAndAssetName(enter.getUserId(), userId, currency);
		//todo 待提纯
		if(userAccount== null) {
			Accounts userB = new Accounts();
			userB.setStatus(AccountsStatus.FICTITIOUS);
			userB.setAssetId(asset.getId());
			userB.setAssetName(asset.getCode());
			userB.setUserId(enter.getUserId());
			userB.setReflexId(userId);
			userB.setOwnerId(enter.getAccountOwnerId());
			userB.setBalance(BigDecimal.ZERO);
			userB.setFrozen(BigDecimal.ZERO);
			userB.setOverdraft(BigDecimal.ZERO);
			userB.setAccountVersion(0);
			userAccount = accountsRepository.save(userB);
		}

		result.put("result",true);
		result.put("userAccount", userAccountInfo(userAccount));
		return result;
		*/
		return null;
	}

	@Deprecated
//	@Override
	@Transactional
	public Map<String, Object> userAccountList(Map<String, Object> jsonParams) throws Exception {
		/*
		Long enterpriseId = Long.parseLong(jsonParams.get("enterpriseId").toString());
		String userId = jsonParams.get("userId").toString();
		Enterprise enter = enterpriseRepository.getOne(enterpriseId);
		Map<String,Object> result = new HashMap<>();
		if(null == enter) {
			result.put("errorCode","ENTERPRISE_NOT_FOUND");
			result.put("result",false);
			return result;
		}
		String valueOfCoinName =  (String)jsonParams.get("valueOfCoinName");
		List<Asset> assets = assetRepository.findAll();
		Asset valueOfCoinAsset = valueOfCoinName == null ? null
				: assets.stream().filter(v->v.getCode().equals(valueOfCoinName)).findFirst().orElse(null);
		if( valueOfCoinName != null && null == valueOfCoinAsset) {
			result.put("errorCode","ASSET_NOT_EXIST");
			result.put("result",false);
			return result;
		}

		List<Accounts> accounts = accountsRepository.findByUserIdAndReflexId(enter.getUserId(), userId);
		BigDecimal sumCny = accounts.stream()
				.map(v->{
					Asset a = assets.stream().filter(vv->vv.getId() == v.getAssetId()).findFirst().orElse(null);
					return v.getBalance().multiply(a.getCnyRate());
				}).reduce(BigDecimal.ZERO,BigDecimal::add);
		result.put("list",accounts.stream().map(v->userAccountInfo(v)).toArray());
		if( valueOfCoinName != null )
			result.put("sumValueOfCoinName",sumCny.divide(valueOfCoinAsset.getCnyRate(),20, RoundingMode.FLOOR).toPlainString());
		result.put("sumValueOfCny",sumCny.toPlainString());
		result.put("result",true);
		return result;
		*/
		return null;
	}

//	@Override
	@Transactional
	public Map<String, Object> executeWithdrawal(Long id,Boolean ok) throws Exception{
		/*
		Object serverSignal = accountRedisson.getBucket(serverPauseSignalRedisKey).get();
		final Object serverSignalInfo = accountRedisson.getBucket(serverPauseSignalRedisInfoKey).get();
		serverSignal = (null == serverSignal || "".equals(serverSignal.toString().trim()))
				? 0 : Integer.parseInt(serverSignal.toString().trim());
		if(!serverSignal.equals(0))return new HashMap<String,Object>()
		{{
			put(CHAINX_SERVICE_PAUSING, true);
			put(CHAINX_SERVICE_PAUSING_INFO, serverSignalInfo != null ?serverSignalInfo.toString():"");
		}};

		if(ok) withdrawalExecutor.agreeWithdrawal(id);
		else withdrawalExecutor.refuseWithdrawal(id);

		Map<String,Object> result = new HashMap<>();
		Withdrawal withdrawal = withdrawalRepository.getOne(id);
		result.put("status",withdrawal.getStatus());
		return result;
		*/
		return null;
	}

//	@Transactional
//	public Map<String, Object> withdrawalBackMoney(Long id) throws Exception{
//		Object serverSignal = accountRedisson.getBucket(serverPauseSignalRedisKey).get();
//		serverSignal = (null == serverSignal || "".equals(serverSignal.toString().trim()))
//				? 0 : Integer.parseInt(serverSignal.toString().trim());
//		if(!serverSignal.equals(0))return new HashMap<String,Object>()
//		{{
//			put(CHAINX_SERVICE_PAUSING, true);
//		}};
//
//		withdrawalExecutor.withdrawBackMoney(id);
//
//		Map<String,Object> result = new HashMap<>();
//		Withdrawal withdrawal = withdrawalRepository.getOne(id);
//		result.put("status",withdrawal.getStatus());
//		return result;
//	}
	
//	@Override
	@Transactional
	public Map<String,Object> accountRechargeAdjustment(Map<String,Object> jsonParams) throws Exception{
		/*
		Object serverSignal = accountRedisson.getBucket(serverPauseSignalRedisKey).get();
		serverSignal = (null == serverSignal || "".equals(serverSignal.toString().trim()))
				? 0 : Integer.parseInt(serverSignal.toString().trim());
		if(!serverSignal.equals(0))return new HashMap<String,Object>()
		{{
			put(CHAINX_SERVICE_PAUSING, true);
		}};

		Long enterpriseId = Long.parseLong(jsonParams.get("enterpriseId").toString());
		String reflexId = jsonParams.get("reflexId").toString();
		String applicationId =  jsonParams.get("applicationId").toString();
		BigDecimal amount = new BigDecimal(jsonParams.get("amount").toString());
		String currency = jsonParams.get("currency").toString();
		String orderId = jsonParams.get("orderId").toString();
		String info = jsonParams.get("info").toString();
		Enterprise enter = enterpriseRepository.getOne(enterpriseId);
		Asset asset = assetRepository.findByCode(currency);
		Application application = applicationRepository.getOne(Long.parseLong(applicationId));
		UserApplication userApplication = userApplicationRepository
				.findByApplicationIdAndReflexIdAndAssetName(application.getId(),reflexId,asset.getCode());
		Accounts accounts = accountsRepository.getOne(
				createChildAccount(userApplication.getUserId(),application.getId(),reflexId,asset.getId()) );
		AccountAdjustment adjustment = new AccountAdjustment();
		adjustment.setOrderId(orderId);
		adjustment.setStatus(AccountAdjustmentStatus.INIT);
		adjustment.setAccountVersion(accounts.getAccountVersion());
		adjustment.setInfo(info);
		adjustment.setRemark("");
		adjustment.setOutTime(3000L);
		adjustment.setFlowAmount(amount);
		adjustment.setCurrency(currency);
		adjustment.setApplicationId(application.getId());
		adjustment.setOwnerId(enter.getAccountOwnerId());
		adjustment.setFlowType(BusinessFlowType.accountAdjustment.name().toString());
		adjustment.setEnterpriseId(enterpriseId);
		adjustment.setEnuserId(reflexId);
		AccountAdjustment ad = chainxServiceBefore.saveAccountAdjustment(adjustment);
		
		DepositExecutor.AccountAdjustmentByAdmin(ad.getId());
		*/
		return null;
	}

	@Transactional
	public Boolean fixBusiness(Long id) throws Exception{
		/*
		检查并恢复business
		0 如果全部都有明确结论，更新bisiness状态即可
		1 列出当前的fund,找到最新一条结论不明确的，且有扣钱队列对应的。（扣钱队列与fund是否有明确结论，是同步的）
		2 将这一条fund，去扣钱队列寻找对应记录。
		如果扣钱队列未完成，删除扣钱队列的那一条。将business的步骤置为此步。并发起addChangeRecord
		FundFlowEvidence keep = fundFlowEvidenceRepository.findByBusinessIdAndStep(bus.getId(),next.getCurrentStep());
		logger.info("消费处理中…… 继续下一步 id "+ next.getId()+" 凭条 : "+keep.getId());
		accountService.addChangeRecord("", JSONObject.toJSONString(keep));
		3 如果没有这样的fund，且将step置为最后一条有结论者。调度redis队列
		*/

		return true;
	}

	@Transactional
	public Boolean closeBusiness(Long id) throws Exception{
		return true;
	}

//	@Transactional
//    @Override
//	public Long createChildAccount(String username,Long applicationId, String reflexId, String assetName) {
//		Asset asset = assetRepository.findByCode(assetName);
//		User user = userRepository.findByUsername(username);
//		Application application = applicationRepository.getOne(applicationId);
//		return createChildAccount(user,application,reflexId,asset);
//    }

//    @Transactional
//    @Override
//	public Long createChildAccount(Long userId,Long applicationId, String reflexId, Long assetId){
//		Asset asset = assetRepository.getOne(assetId);
//		User user = userRepository.getOne(userId);
//		Application application = applicationRepository.getOne(applicationId);
//		return createChildAccount(user,application,reflexId,asset);
//    }

//    @Transactional
//    protected Long createChildAccount(User user,Application application, String reflexId, Asset asset){
//		return chainxServiceBefore.createChildAccount( user,  application,  reflexId,  asset);
//	}

}
