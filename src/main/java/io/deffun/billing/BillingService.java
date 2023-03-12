package io.deffun.billing;

import io.deffun.ProjectRepository;
import io.deffun.usermgmt.UserEntity;
import io.deffun.usermgmt.UserRepository;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Singleton
public class BillingService {
    private static final Logger LOG = LoggerFactory.getLogger(BillingService.class);

    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private UserRepository userRepository;
    @Value("${deffun.billing.rubRate}")
    private String hourRate;

    @Transactional
    public void updateBalance(Long id) {
        chargeForHours(id, 1L);
    }

    @Transactional
    public void chargeForHours(Long id, long hours) {
        projectRepository.findById(id)
                .ifPresentOrElse(project -> {
                    if (project.getApiEndpointUrl() != null) {
                        LOG.info("Hourly balance update for project '{}' (ID {})", project.getName(), project.getId());
                        UserEntity user = project.getUser();
                        BigDecimal subtrahend = new BigDecimal(hourRate);
                        BigDecimal multiply = subtrahend.multiply(new BigDecimal(hours));
                        user.setBalance(user.getBalance().subtract(multiply));
                        userRepository.update(user);
                        project.setLastCharge(LocalDateTime.now());
                        projectRepository.update(project);
                    }
                }, () -> LOG.info("Project with ID {} probably was deleted", id));
    }
}
