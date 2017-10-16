package de.papke.cloud.portal.model;

import java.util.List;

/**
 * Created by chris on 16.10.17.
 */
public class Data {

    private String applicationTitle;
    private String username;
    private List<String> cloudProviderList;

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public void setApplicationTitle(String applicationTitle) {
        this.applicationTitle = applicationTitle;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getCloudProviderList() {
        return cloudProviderList;
    }

    public void setCloudProviderList(List<String> cloudProviderList) {
        this.cloudProviderList = cloudProviderList;
    }
}
