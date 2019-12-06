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
    @Value("${ADMIN_USER_NAME:admin}")
    private String adminUserName;
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createAccountForPayment(Long userId, String reflexId, String target, BigDecimal amount, String currency,
            String orderId, String remark) throws Exception {
        Map<String, Object> admin = userService.findUserByKeyword(adminUserName);
        if(admin==null)return null;
        Long adminUserId = Long.parseLong(admin.get("id").toString());
        AssetEntity asset = assetRepository.findByCurrency(currency);

        accountCreateExecutor.createAccount(userId, reflexId, asset);
        accountCreateExecutor.createAccount(adminUserId, target, asset);

        return adminUserId;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentEntity payment(Long adminUserId,Long userId, String reflexId, String target, BigDecimal amount, String currency,
            String orderId, String remark) throws Exception {

        AccountEntity fromAccount = accountsRepository.findByUserIdAndReflexIdAndCurrency(userId,reflexId,currency);
        AccountEntity toAccount = accountsRepository.findByUserIdAndReflexIdAndCurrency(adminUserId,target,currency);

        return payment(fromAccount.getId(), toAccount.getId(), userId, target, amount, currency, orderId, remark);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentEntity payment(long accountIdFrom, long accountIdTo, Long userId, String target, BigDecimal amount,
            String currency, String orderId, String remark) throws Exception {

//		final String type = BusinessFlowType.payAccount.name();
//		int step = 2;
        AccountEntity fromAccount = accountsRepository.getOne(accountIdFrom);
        AccountEntity toAccount = accountsRepository.getOne(accountIdTo);
        if(fromAccount.getBalance().add(amount).compareTo(BigDecimal.ZERO)<0)
            return null;
        if(toAccount.getBalance().add(amount.negate()).compareTo(BigDecimal.ZERO)<0)
            return null;

        PaymentEntity paymentEntity = paymentRepository.findByOrderId(orderId);
        if (null != paymentEntity) return paymentEntity;

        paymentEntity = new PaymentEntity();

        paymentEntity.setOrderId(orderId);
        paymentEntity.setCurrency(currency);
        //后面有重复，待调整
        paymentEntity.setFromAccountId(accountIdFrom);
        paymentEntity.setToAccountId(accountIdTo);
        paymentEntity.setStatus(PaymentStatus.INIT);
        paymentEntity.setOutTime(System.currentTimeMillis()+3000L);
        paymentEntity.setAmount(amount);
        paymentEntity.setCurrency(currency);
        paymentEntity.setRemark(remark);
        paymentEntity.setUserId(userId);
        paymentEntity.setTarget(target);
        paymentEntity.setBusinessId(0L);
        paymentEntity.setCreatedAt(System.currentTimeMillis());
        paymentEntity.setUpdatedAt(System.currentTimeMillis());
        paymentEntity.setVersion(0);
        return paymentRepository.save(paymentEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusinessEntity createBusinessForPayment(Long id) throws Exception {
        PaymentEntity paymentEntity = paymentRepository.getOne(id);
        List<FundFlowDto> funds = new ArrayList<>(2);
        //扣费一定要先作
        if (paymentEntity.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            funds.add(new FundFlowDto(paymentEntity.getToAccountId(), paymentEntity.getAmount().negate()));
            funds.add(new FundFlowDto(paymentEntity.getFromAccountId(), paymentEntity.getAmount()));
        } else {
            funds.add(new FundFlowDto(paymentEntity.getFromAccountId(), paymentEntity.getAmount()));
            funds.add(new FundFlowDto(paymentEntity.getToAccountId(), paymentEntity.getAmount().negate()));
        }

        BusinessEntity businessEntity = businessExecutor.createBusiness(paymentEntity.getOrderId(), BusinessFlowType.payAccount,
                paymentEntity.getId(), funds, paymentEntity.getRemark());

        paymentEntity.setBusinessId(businessEntity.getId());
        paymentRepository.save(paymentEntity);

        return businessEntity;
    }
}
