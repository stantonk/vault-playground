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

    static final String role_id = "66bce093-45f5-814f-6d27-46efafee75bf";
    static final String secret_id = "971db68e-d146-510b-4729-6fdfe403efed";

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

        try {
            String token = vault.auth().loginByAppRole("approle", role_id, secret_id).getAuthClientToken();
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
            Map<String, String> creds = temporaryDatabaseCreds.getData();

            DBI dbi = new DBI("jdbc:mysql://127.0.0.1:3306/mydb",
                    temporaryDatabaseCreds.getData().get("username"), temporaryDatabaseCreds.getData().get("password"));
            List<Map<String, Object>> cars = dbi.withHandle(h -> h.select("SELECT * from Cars;"));
            logger.info("---- Creds used username={} password={} ----", creds.get("username"), creds.get("password"));
            for (Map<String, Object> car : cars) {
                logger.info("{}", car);
            }

            Thread.sleep(5000l);
        }

    }

}
