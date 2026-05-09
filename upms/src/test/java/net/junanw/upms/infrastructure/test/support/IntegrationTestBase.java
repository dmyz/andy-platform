package net.junanw.upms.infrastructure.test.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestSaTokenDaoConfig.class)
@Transactional
public abstract class IntegrationTestBase {
}
