package software.amazon.servicecatalog;

import software.amazon.awssdk.services.servicecatalog.ServiceCatalogClient;
import software.amazon.cloudformation.LambdaWrapper;

public class SCClientBuilder {

    public static ServiceCatalogClient getClient() {
        return ServiceCatalogClient
                .builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}

