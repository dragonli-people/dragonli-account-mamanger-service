//待整理

//package org.dragonli.service.modules.accountmanagerservice.executor;
//
//import com.alibaba.dubbo.config.annotation.Reference;
//import org.dragonli.service.modules.account.interfaces.AccountChangeService;
//import org.dragonli.service.modules.accountservice.entity.enums.*;
//import org.dragonli.service.modules.accountservice.entity.models.*;
//import org.dragonli.service.modules.accountservice.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class OtherExecutor {
//
////	@Autowired
////	ChainXService chainxService;
//
//    @Reference
//    AccountChangeService accountChangeService;
//
//    @Autowired
//    PaymentRepository paymentRepository;
//
//    @Autowired
//    FundFlowEvidenceRepository fundFlowEvidenceRepository;
//
//    @Autowired
//    BusinessRepository businessRepository;
//
//    @Autowired
//    OtherExecutor chainxServiceBefore;
//
//    @Autowired
//    AccountsRepository accountsRepository;
//
//    @Autowired
//    AssetRepository assetRepository;
//
//    @Autowired
//    WithdrawalRepository withdrawalRepository;
//
//    @Autowired
//    AccountAdjustmentRepository accountAdjustmentRepository;
//
//    @Autowired
//    AccountAssetsRecordRepository accountAssetsRecordRepository;
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public FundFlowEvidenceEntity savePayAccountFund(FundFlowEvidenceEntity fund) {
//        FundFlowEvidenceEntity res = fundFlowEvidenceRepository.save(fund);
//        return res;
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public List<FundFlowEvidenceEntity> savePayAccountFundBatch(List<FundFlowEvidenceEntity> funds) {
//        return fundFlowEvidenceRepository.saveAll(funds);
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public PaymentEntity addTransfer(PaymentEntity payment, Long faccountId, Long taccountId) {
////		Long faccountId = jsonParams.getLong("faccountId"); //企业子账户ID
////		Long taccountId = jsonParams.getLong("taccountId"); //企业用户虚拟子账户
//
//        PaymentEntity result = paymentRepository.save(payment);
//        return result;
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public Long addWithdrawal(WithdrawalEntity withdrawal) {
//        withdrawal = withdrawalRepository.save(withdrawal);
//        withdrawalRepository.refresh(withdrawal);
//        return withdrawal.getId();
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public Long createBusinessAndResultForNext(BusinessEntity business, Long referenceId, String orderId, BusinessFlowType flowType, int step, String tokenUrl) {
//        if (business == null) business = new BusinessEntity();
//
//        BusinessEntity bus = businessRepository.findByOrderId(orderId);
//        if (null != bus) return null;
//        business.setCurrentStep(1);
//        business.setType(flowType);
//        business.setSteps(step);
//        business.setOrderId(orderId);
//        business.setReferenceId(referenceId);
//        business.setStatus(BusinessStatus.INIT);
//        business = businessRepository.save(business);
//
//
//        return business.getId();
//    }
//
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public AccountAdjustmentEntity saveAccountAdjustment(AccountAdjustmentEntity fund) {
//        AccountAdjustmentEntity res = accountAdjustmentRepository.save(fund);
//        return res;
//    }
//
//    /*
//    @Transactional
//    public Integer fixBusiness(Long id) throws Exception {
//
//        BusinessEntity business = businessRepository.getOne(id);
//        if (null == business) return null;
//        List<FundFlowEvidenceEntity> fundFlowEvidences = fundFlowEvidenceRepository.findByBusinessIdAndStepAfter(id, 0);
//        if (fundFlowEvidences.size() != 0 && fundFlowEvidences.size() != business.getSteps())
//            throw new Exception("WARN::business data err!fund count not eqauls step");//否则没插入过，或者应该指批量成功
//
//        if (fundFlowEvidences.size() == 0)
//            return null;//特别情况，不必修复
//
//        FundFlowEvidenceEntity firstNotFinal = fundFlowEvidences.stream()
//                .filter(v -> !v.getFlowStatus().isFinalStatus).findFirst().orElse(null);
//
//        if (null == firstNotFinal) return business.getSteps();
//
//        //删除还没执行扣钱队列。不过目前观测，并无这种情况
//        for (FundFlowEvidenceEntity ff : fundFlowEvidences) {
//            if (ff.getStep() < firstNotFinal.getStep()) continue;
//            AccountAssetsRecordEntity aa = accountAssetsRecordRepository.findFirstByOrderId(ff.getOrderId());
//            if (null == aa) continue;
//            if (AccountAssetsRecordStatus.INIT != aa.getRecordStatus())
//                throw new Exception("WARN::AccountAssetsRecordStatus is not init and fund status is INIT! ");
//            accountAssetsRecordRepository.delete(aa);
//        }
//
//        return firstNotFinal.getStep();
//    }
//    */
//
//}
