package org.dragonli.service.modules.accountmanagerservice.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import org.dragonli.service.general.interfaces.general.OtherService;
import org.dragonli.service.modules.account.interfaces.AccountChangeService;
import org.dragonli.service.modules.account.interfaces.AccountManagerService;
import org.dragonli.service.modules.accountmanagerservice.executor.*;
import org.dragonli.service.modules.accountservice.entity.enums.AccountAdjustmentStatus;
import org.dragonli.service.modules.accountservice.entity.enums.PaymentStatus;
import org.dragonli.service.modules.accountservice.entity.models.*;
import org.dragonli.service.modules.accountservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service(interfaceClass = AccountManagerService.class, register = true, timeout = 150000000, retries = -1, delay = -1)
public class AccountManagerServiceImpl implements AccountManagerService {
    final Logger logger = LoggerFactory.getLogger(getClass());
    @Reference
    AccountChangeService accountChangeService;
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
    AccountCreateExecutor accountCreateExecutor;
    @Autowired
    AdjustmentExecutor adjustmentExecutor;
    @Autowired
    BusinessExecutor businessExecutor;

//    @Value("${spring.dubbo-jackpot-service.info.redis.chainxServerPauseSignal}")
//    String serverPauseSignalRedisKey;
//    @Value("${spring.dubbo-jackpot-service.info.redis.chainxServerPauseSignalInfo}")
//    String serverPauseSignalRedisInfoKey;
//    private final String CHAINX_SERVICE_PAUSING = "CHAINX_SERVICE_PAUSING";
//    private final String CHAINX_SERVICE_PAUSING_INFO = "CHAINX_SERVICE_PAUSING_INFO";

    @Transactional
    @Override
    public Map<String, Object> withdrawal(Long userId, String reflexId, String currency,
            String amountStr) throws Exception {
        // TODO Auto-generated method stub
        BigDecimal amount = new BigDecimal(amountStr);
//		Object serverSignal = jedisPool.getBucket(serverPauseSignalRedisKey).get();
//		serverSignal = (null == serverSignal || "".equals(serverSignal.toString().trim()))
//				? 0 : Integer.parseInt(serverSignal.toString().trim());
//		if(!serverSignal.equals(0))return null;
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Map<String, Object> paymentStatus(String orderId) throws Exception {
        JSONObject result = new JSONObject();
        PaymentStatus status = Optional.ofNullable(paymentRepository.findByOrderId(orderId)).orElseThrow(
                () -> new Exception("cant find order id")).getStatus();
        result.put("isFinish", status.isFinished());
        result.put("status", status.name());
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Map<String, Object> adjustmentStatus(String orderId) throws Exception {
        JSONObject result = new JSONObject();
        AccountAdjustmentStatus status = Optional.ofNullable(
                accountAdjustmentRepository.findFirstByOrderId(orderId)).orElseThrow(
                () -> new Exception("cant find order id")).getStatus();
        result.put("isFinish", status.isFinished());
        result.put("status", status.name());
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String payment(Long userId, String reflexId, String target, String amountStr, String currency,
            String orderId, String remark, Boolean readOnly) throws Exception {
        //todo
        BigDecimal amount = new BigDecimal(amountStr);
//		Object serverSignalSource = jedisPool.getBucket(serverPauseSignalRedisKey).get();
//		final Object serverSignalInfo = jedisPool.getBucket(serverPauseSignalRedisInfoKey).get();
//		final int serverSignal = (null == serverSignalSource || "".equals(serverSignalSource.toString().trim()))
//				? 0 : Integer.parseInt(serverSignalSource.toString().trim());
//		if(serverSignal != 0)return new HashMap<String,Object>()
//		{{
//			put(CHAINX_SERVICE_PAUSING, serverSignal);
//			put(CHAINX_SERVICE_PAUSING_INFO, serverSignalInfo != null ?serverSignalInfo.toString():"");
//		}};

        if (readOnly != null && readOnly) {
            PaymentEntity paymentEntity = paymentRepository.findByOrderId(orderId);
            return paymentEntity == null ? null : paymentEntity.getStatus().name();
//            return transfer == null ? null : transfer.getStatus().name();
        }
        Long adminUserId = paymentExecutor.createAccountForPayment(userId, reflexId, target, amount, currency, orderId,
                remark);
        PaymentEntity paymentEntity = paymentExecutor.payment(adminUserId, userId, reflexId, target, amount, currency,
                orderId, remark);
        if (paymentEntity == null) return null;
        paymentEntity = paymentRepository.get(paymentEntity.getId());
        BusinessEntity businessEntity = paymentExecutor.createBusinessForPayment(paymentEntity.getId());
//        System.out.println("2 accountChangeService == null ?"+(accountChangeService==null));
        businessExecutor.beginFundForBusiness(businessEntity.getId());
        return paymentEntity.getStatus().name();

//        Map<String,Object> body = chainxServicePayment.payment(username,applicationId,reflexId,amount,currency,
//        orderId,tokenUrl,remark);
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

    @Override
    @Transactional
    public Map<String, Object> accountWithdrawal(String userId, String amountStr, String currency, String orderId,
            String address, String addressExtend) throws Exception {
        BigDecimal amount = new BigDecimal(amountStr);
		/*
		Object serverSignalSource = jedisPool.getBucket(serverPauseSignalRedisKey).get();
		final Object serverSignalInfo = jedisPool.getBucket(serverPauseSignalRedisInfoKey).get();
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
		JSONObject json = accountChangeService.addChangeRecord("", res);
		json.put(CHAINX_SERVICE_PAUSING,serverSignal);
		json.put(CHAINX_SERVICE_PAUSING_INFO, serverSignalInfo != null ?serverSignalInfo.toString():"");
		return json;
		*/
        return null;
    }

    @Deprecated
    @Override
    @Transactional
    public Map<String, Object> getUserAccount(Long userId, String reflexId, String currency) throws Exception {
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

		Accounts userAccount = accountsRepository.findByUserIdAndReflexIdAndAssetName(enter.getUserId(), userId,
		currency);
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
    @Override
    @Transactional
    public Map<String, Object> userAccountList(Long userId) throws Exception {
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
			result.put("sumValueOfCoinName",sumCny.divide(valueOfCoinAsset.getCnyRate(),20, RoundingMode.FLOOR)
			.toPlainString());
		result.put("sumValueOfCny",sumCny.toPlainString());
		result.put("result",true);
		return result;
		*/
        return null;
    }

    @Override
    @Transactional
    public Map<String, Object> executeWithdrawal(Long id, Boolean ok) throws Exception {
		/*
		Object serverSignal = jedisPool.getBucket(serverPauseSignalRedisKey).get();
		final Object serverSignalInfo = jedisPool.getBucket(serverPauseSignalRedisInfoKey).get();
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
//		Object serverSignal = jedisPool.getBucket(serverPauseSignalRedisKey).get();
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

    @Override
    @Transactional
    public String accountAdjustment(String orderId, Long userId, String reflexId, String currency, String amountStr,
            String remark) throws Exception {
        BigDecimal amount = new BigDecimal(amountStr);
        AccountAdjustmentEntity adjustment = accountAdjustmentRepository.findFirstByOrderId(orderId);
        if (adjustment != null) return adjustment.getStatus().name();
        AssetEntity asset = assetRepository.findByCurrency(currency);
        Long accountId = accountCreateExecutor.createAccount(userId, reflexId, asset);
//        accountsRepository.flush();
//        AccountEntity account = accountsRepository.getOne( accountId );

        adjustment = new AccountAdjustmentEntity();
        adjustment.setOrderId(orderId);
        adjustment.setStatus(AccountAdjustmentStatus.INIT);
        adjustment.setInfo("{}");
        adjustment.setRemark(remark);
        adjustment.setOutTime(System.currentTimeMillis()+3000L);
        adjustment.setAccountId(accountId);
        adjustment.setFlowAmount(amount);
        adjustment.setUserId(userId);
        adjustment.setBusinessId(0L);
        adjustment.setCurrency(currency);
        adjustment.setReflexId(reflexId);
        adjustment.setCreatedAt(System.currentTimeMillis());
        adjustment.setUpdatedAt(System.currentTimeMillis());
        adjustment.setVersion(0);

        adjustment = adjustmentExecutor.saveOne(adjustment);

        BusinessEntity businessEntity = adjustmentExecutor.createBusinessForAdjustment(adjustment.getId());
        businessExecutor.beginFundForBusiness(businessEntity.getId());
        return adjustment.getStatus().name();
    }

    @Override
    @Transactional
    public Boolean fixBusiness(Long id) throws Exception {
		/*
		检查并恢复business
		0 如果全部都有明确结论，更新bisiness状态即可
		1 列出当前的fund,找到最新一条结论不明确的，且有扣钱队列对应的。（扣钱队列与fund是否有明确结论，是同步的）
		2 将这一条fund，去扣钱队列寻找对应记录。
		如果扣钱队列未完成，删除扣钱队列的那一条。将business的步骤置为此步。并发起addChangeRecord
		FundFlowEvidence keep = fundFlowEvidenceRepository.findByBusinessIdAndStep(bus.getId(),next.getCurrentStep());
		logger.info("消费处理中…… 继续下一步 id "+ next.getId()+" 凭条 : "+keep.getId());
		accountChangeService.addChangeRecord("", JSONObject.toJSONString(keep));
		3 如果没有这样的fund，且将step置为最后一条有结论者。调度redis队列
		*/

        return true;
    }

    @Override
    @Transactional
    public Boolean closeBusiness(Long id) throws Exception {
        return true;
    }
}
