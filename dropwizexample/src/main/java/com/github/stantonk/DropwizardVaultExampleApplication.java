package com.github.stantonk;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DropwizardVaultExampleApplication extends Application<DropwizardVaultExampleConfiguration> {

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
                    final Environment environment) throws VaultException {
            VaultConfig config = new VaultConfig()
                    .address("http://127.0.0.1:8200")
                    .token("myroot")
                    .openTimeout(3)
                    .readTimeout(30)
                    .build();
        Vault vault = new Vault(config);

        String roleId = "";
        String secretId = "";
        String token = vault.auth().loginByAppRole("approle", roleId, secretId).getAuthClientToken();
        vault.g
    }

}
