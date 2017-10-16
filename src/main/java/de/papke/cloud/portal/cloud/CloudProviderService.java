package de.papke.cloud.portal.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 16.10.17.
 */
@Service
public class CloudProviderService {

    private static final Logger LOG = LoggerFactory.getLogger(CloudProviderService.class);

    private List<String> providerList = new ArrayList<>();

    @PostConstruct
    public void init() {

        try {
            URL url = getClass().getClassLoader().getResource("terraform");
            File terraformFolder = new File(url.toURI());
            if (!terraformFolder.isFile()) {
                File[] providerFolderArray = terraformFolder.listFiles();
                for (File providerFolder : providerFolderArray) {
                    providerList.add(providerFolder.getName());
                }
            }
        }
        catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public List<String> getProviderList() {
        return providerList;
    }
}
