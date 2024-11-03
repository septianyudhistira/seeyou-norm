package com.seeyou.dieselpoint.norm.entities;

import java.io.Serializable;
import java.util.*;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */
public class AppMenu implements Serializable {
    private Map<String, List<String>>   actionByPath;
    private List<Map<String,Object>>    leftMenu;
    private Set<String>                 paths;
    private String                      jsonWebLeftMenu;
    private String                      jsonMobileLeftMenu;
    private String                      homeLink;
    private Map<String, String>         menuByUrl;
    private List<String>                dashboardCodes;
    private List<String>                qrCodePaths;


    public Map<String, List<String>> getActionByPath() {
        return actionByPath;
    }

    public void setActionByPath(Map<String, List<String>> actionByPath) {
        this.actionByPath = new HashMap<String, List<String>>();

        for (String keySet : actionByPath.keySet()) {
            final String        action  = actionByPath.get(keySet).toString();
            final char[]        chrs    = action.toCharArray();
            final List<String>  actions = new ArrayList<String>();

            for (char chr : chrs) {
                if (chr == 'C') {
                    actions.add("CREATE");
                }

                if (chr == 'R') {
                    actions.add("READ");
                    actions.add("INQ");
                    actions.add("ADV");
                    actions.add("2100");
                    actions.add("2400");
                }

                if (chr == 'U') {
                    actions.add("UPDATE");
                    actions.add("PAY");
                    actions.add("2200");
                }

                if (chr == 'O') {
                    actions.add("DELETE");
                }
            }

            this.actionByPath.put(keySet, actions);
        }
    }

    public List<Map<String, Object>> getLeftMenu() {
        return leftMenu;
    }

    public void setLeftMenu(List<Map<String, Object>> leftMenu) {
        this.leftMenu = leftMenu;
    }

    public Set<String> getPaths() {
        return paths;
    }

    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    public String getJsonLeftMenu(final String channel) {
        return channel.equals("WEB") ? jsonWebLeftMenu : jsonMobileLeftMenu;
    }

    public String getJsonWebLeftMenu() {
        return jsonWebLeftMenu;
    }

    public void setJsonWebLeftMenu(String jsonWebLeftMenu) {
        this.jsonWebLeftMenu = jsonWebLeftMenu;
    }

    public String getJsonMobileLeftMenu() {
        return jsonMobileLeftMenu;
    }

    public void setJsonMobileLeftMenu(String jsonMobileLeftMenu) {
        this.jsonMobileLeftMenu = jsonMobileLeftMenu;
    }

    public String getHomeLink() {
        return homeLink;
    }

    public void setHomeLink(String homeLink) {
        this.homeLink = homeLink;
    }

    public Map<String, String> getMenuByUrl() {
        return menuByUrl;
    }

    public void setMenuByUrl(Map<String, String> menuByUrl) {
        this.menuByUrl = menuByUrl;
    }

    public List<String> getDashboardCodes() {
        return dashboardCodes;
    }

    public void setDashboardCodes(List<String> dashboardCodes) {
        this.dashboardCodes = dashboardCodes;
    }

    public List<String> getQrCodePaths() {
        return qrCodePaths;
    }

    public void setQrCodePaths(List<String> qrCodePaths) {
        this.qrCodePaths = qrCodePaths;
    }

    public void removeAction(final String moduleFeId) {
        actionByPath.remove(moduleFeId);
    }

    public boolean getValidationAction(final String type, final String path) throws Exception {
        boolean isContains = false;

        for (String keySet : actionByPath.keySet()) {
            final List<String> actions = actionByPath.get(keySet);
            isContains = actions.contains(type.toUpperCase());

            if (isContains) {
                break;
            }
        }

        if (!isContains) {
            throw new Exception("Invalid type : " + type + "");
        }
        return isContains;
    }
}
