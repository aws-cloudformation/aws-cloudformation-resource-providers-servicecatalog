package software.amazon.servicecatalog;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.services.servicecatalog.ServiceCatalogClient;
import software.amazon.cloudformation.LambdaWrapper;

import static software.amazon.awssdk.core.client.config.SdkAdvancedClientOption.USER_AGENT_SUFFIX;

public class SCClientBuilder {
    private static final String CFN_USER_AGENT_SUFFIX = "CFN_Resource_Generated";

    public static ServiceCatalogClient getClient() {
        final ClientOverrideConfiguration overrideConfiguration = ClientOverrideConfiguration
                .builder()
                .putAdvancedOption(USER_AGENT_SUFFIX, CFN_USER_AGENT_SUFFIX)
                .build();
        return ServiceCatalogClient
                .builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .overrideConfiguration(overrideConfiguration)
                .build();
    }
}
