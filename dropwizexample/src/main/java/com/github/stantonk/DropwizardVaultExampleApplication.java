package com.github.stantonk;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DropwizardVaultExampleApplication extends Application<DropwizardVaultExampleConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(DropwizardVaultExampleApplication.class);

    public static void main(final String[] args) throws Exception {
        new DropwizardVaultExampleApplication().run(args);
    }

    @Override
    public String getName() {
        return "DropwizardVaultExample";
    }

    @Override
    public void initialize(final Bootstrap<DropwizardVaultExampleConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final DropwizardVaultExampleConfiguration configuration,
                    final Environment environment) throws InterruptedException {
        VaultConfig config = null;
        try {
            config = new VaultConfig()
                    .address("http://127.0.0.1:8200")
                    .token("myroot")
                    .openTimeout(3)
                    .engineVersion(1)
                    .readTimeout(30)
                    .build();
        } catch (VaultException e) {
            logger.error("couldn't connect to vault {}", e.getMessage(), e);
            System.exit(1);
        }
        Vault vault = new Vault(config);

        String roleId = "2ad3264a-af8e-103b-236d-e60b740185d2";
        String secretId = "06b47e30-712f-2c11-6ccb-224e9ed5f394";
        try {
            String token = vault.auth().loginByAppRole("approle", roleId, secretId).getAuthClientToken();
        } catch (VaultException e) {
            logger.error("couldn't login by AppRole to vault {}", e.getMessage(), e);
            System.exit(1);
        }

        // loop forever talking to database while getting fresh creds from vault...
        while (true) {

            // get creds
            LogicalResponse temporaryDatabaseCreds = null;
            try {
                temporaryDatabaseCreds = vault.logical().read("database/creds/my-role");
            } catch (VaultException e) {
                logger.error("couldn't get mysql credentials from vault {}", e.getMessage(), e);
                System.out.println(e.getMessage());
                System.exit(1);
            }
            logger.info("temporaryDatabaseCreds = {}", temporaryDatabaseCreds.getData());
            DBI dbi = new DBI("jdbc:mysql://127.0.0.1:3306/mydb",
                    temporaryDatabaseCreds.getData().get("username"), temporaryDatabaseCreds.getData().get("password"));
            List<Map<String, Object>> maps = dbi.withHandle(h -> h.select("SELECT 1"));
            logger.info("query results= {}", maps);

            Thread.sleep(5000l);
        }

    }

}
