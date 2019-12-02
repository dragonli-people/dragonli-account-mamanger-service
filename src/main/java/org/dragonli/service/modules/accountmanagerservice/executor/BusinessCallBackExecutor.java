package org.dragonli.service.modules.accountmanagerservice.executor;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONObject;
import org.dragonli.service.modules.account.interfaces.AccountChangeService;
import org.dragonli.service.modules.accountservice.entity.enums.*;
import org.dragonli.service.modules.accountservice.entity.models.*;
import org.dragonli.service.modules.accountservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BusinessCallBackExecutor {

	
	final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@Autowired
    BusinessRepository businessRepository;
	
	@Autowired
    FundFlowEvidenceRepository fundFlowEvidenceRepository;
	
	@Autowired
    PaymentRepository paymentRepository;

	@Autowired
    WithdrawalRepository withdrawalRepository;

    @Autowired
    DepositRepository depositRepository;
	
	@Reference
	AccountChangeService accountService;
	
	@Autowired
    AccountAdjustmentRepository accountAdjustmentRepository;
	/**
	 * 付款
	 */
//	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void receiveRedisInfo(String content) throws Exception{
//		Arrays.asList(evidence.getId(), evidence.getBusinessId(), change,
//				System.currentTimeMillis())
		String[] arr = content.split(",");
//		JSONObject json = JSONObject.parseObject(jsonParams);
		Long evidenceId  = Long.parseLong(arr[0]);//json.getLong("id");
		Long businessId = Long.parseLong(arr[1]);// json.getLong("busId");
		Boolean change = Boolean.parseBoolean(arr[2]);// json.getBoolean("result");
		FundFlowEvidenceEntity currentFund = fundFlowEvidenceRepository.getOne(evidenceId);
		if( currentFund.getFlowStatus().isFinalStatus )return;//必然是已经处理过的。多半因为停服而重新进来
//		JSONObject json = JSONObject.parseObject(content);
		logger.info(" receive account paras change : "+change+" evidenceId : "+evidenceId+" businessId : "+businessId);
		BusinessEntity bus = businessRepository.getOne(businessId);
		
		int result = checkBusinss(bus, currentFund, change);
		if(result == -1 || result == 2)//重复（理论上不会发生）或进行下一步（即未完成）
			return;
		boolean success = result == 0;
		
		if( BusinessFlowType.payAccount == bus.getType())
			receivePayment(bus,evidenceId,change,success);
		else if( BusinessFlowType.accountWithdrawal == bus.getType() )
			receiveWithdrawal(bus,evidenceId,change,success);
        else if( BusinessFlowType.accountDeposit == bus.getType() )
            receiveDeposit(bus,evidenceId,change,success);
		else if( BusinessFlowType.accountWithdrawalBackMoney == bus.getType() )
			receiveWithdrawalBack(bus,evidenceId,change,success);
		else if( BusinessFlowType.accountAdjustment == bus.getType() )
			receiveAccountAdjustment(bus,evidenceId,change,success);
	}

	@Transactional
	public int checkBusinss(BusinessEntity bus,FundFlowEvidenceEntity currentFund,Boolean change) throws Exception{
		int currentStep = bus.getCurrentStep();
//		 = fundFlowEvidenceRepository.getOne(evidenceId);
		if(currentFund.getStep() < currentStep) {
			logger.info("消费ID ："+currentFund.getId()+" 已处理");
			return -1;
		}
		if(!change) {
			logger.info("消费处理中途失败1:"+bus.getOrderId());
			List<FundFlowEvidenceEntity> failedList = fundFlowEvidenceRepository
							.findByBusinessIdAndStepAfter(bus.getId(),currentStep);
			failedList.stream().forEach(failed ->{
				failed.setFlowStatus(EvidenceStatus.FAILED);
//				return failed;
			});
			bus.setStatus(BusinessStatus.FAILED);
			businessRepository.save(bus);
			fundFlowEvidenceRepository.saveAll(failedList);
//			String reason =  currentFund.getFlowStatus().toString();
			return 1;//"failed-finished";
		}
		currentFund.setFlowStatus(EvidenceStatus.SUCCESS);
		currentFund = fundFlowEvidenceRepository.save(currentFund);
		if(bus.getSteps() == bus.getCurrentStep()) {
			//已经完成了，做后续处理就可以了
			bus.setStatus(BusinessStatus.SUCCESS);
			businessRepository.save(bus);
			return 0;//"success-finished";
		}
		bus.setCurrentStep(currentStep+1);
		BusinessEntity next =  businessRepository.save(bus);
		FundFlowEvidenceEntity keep = fundFlowEvidenceRepository.findByBusinessIdAndStep(bus.getId(),next.getCurrentStep());
//		logger.info("消费处理中…… 继续下一步 id "+ next.getId()+" 凭条 : "+keep.getId());
		accountService.addChangeRecord(keep.getId());
		return 2;
	}
	
	@Transactional
	public void receivePayment(BusinessEntity bus,Long evidenceId,Boolean change,boolean success){
		if(!success) {
			PaymentEntity paymentEntity =  paymentRepository.getOne(bus.getReferenceId());
			paymentEntity.setStatus(PaymentStatus.FAILED);
			paymentRepository.save(paymentEntity);
			return ;//"failed-finished";
		}
		//已经完成了，发送http就可以了
		PaymentEntity paymentEntity =  paymentRepository.getOne(bus.getReferenceId());
		paymentEntity.setStatus(PaymentStatus.SUCCESS);
		paymentRepository.save(paymentEntity);
	}

	@Transactional
	public void receiveWithdrawal(BusinessEntity bus,Long evidenceId,Boolean change,boolean success){
		WithdrawalEntity withdrawal = withdrawalRepository.getOne(bus.getReferenceId());
		if(!success) {
			FundFlowEvidenceEntity currentFund = fundFlowEvidenceRepository.getOne(evidenceId);
			withdrawal.setStatus(WithdrawalStatus.DEDUCTION_PAYMENT_FAILD);
			withdrawal.setRemark(currentFund.getFlowStatus().toString());
			withdrawalRepository.save(withdrawal);
			return;
		}
		
		if( WithdrawalStatus.CHILD_HAD_TRASFER == withdrawal.getStatus() )
		{
			//企业已批准并将子帐户的钱销掉，转入母帐户，再从母帐户销掉。此步异步完成之后，将状态置为
			withdrawal.setStatus(WithdrawalStatus.HAD_DEDUCTION_AND_WAIT);
			withdrawalRepository.save(withdrawal);
		}
	}

	@Transactional
    public void receiveDeposit(BusinessEntity bus,Long evidenceId,Boolean change,boolean success){
		DepositEntity deposit = depositRepository.getOne(bus.getReferenceId());
		if(!success) {
			FundFlowEvidenceEntity currentFund = fundFlowEvidenceRepository.getOne(evidenceId);
			deposit.setDepositStatus(DepositStatus.PAYMENT_FAILD);
			deposit.setRemark(currentFund.getFlowStatus().toString());
			depositRepository.save(deposit);
			return;
		}
        deposit.setDepositStatus(DepositStatus.SUCCESS);
        depositRepository.save(deposit);
    }

	@Transactional
	public void receiveWithdrawalBack(BusinessEntity bus,Long evidenceId,Boolean change,boolean success){
		WithdrawalEntity withdrawal = withdrawalRepository.getOne(bus.getReferenceId());
		if(!success) {
			FundFlowEvidenceEntity currentFund = fundFlowEvidenceRepository.getOne(evidenceId);
			withdrawal.setStatus(WithdrawalStatus.FAILED);
			withdrawal.setRemark(currentFund.getFlowStatus().toString());
			withdrawalRepository.save(withdrawal);
			return;
		}
		withdrawal.setStatus(WithdrawalStatus.BACK_MONEY_PAYMENT_FAILD);
		withdrawalRepository.save(withdrawal);
	}
	
	@Transactional
	public void receiveAccountAdjustment(BusinessEntity bus,Long evidenceId,Boolean change,boolean success) {
//		System.out.println("buss===========>"+bus.getId());
		AccountAdjustmentEntity adjustment = accountAdjustmentRepository.getOne(bus.getReferenceId());
		if(!success) {
			FundFlowEvidenceEntity currentFund = fundFlowEvidenceRepository.getOne(evidenceId);
			adjustment.setStatus(AccountAdjustmentStatus.FAILED);
			adjustment.setRemark(currentFund.getFlowStatus().toString());
			accountAdjustmentRepository.save(adjustment);
			return;
		}
		adjustment.setStatus(AccountAdjustmentStatus.SUCCESS);
		accountAdjustmentRepository.save(adjustment);
	}


}
