package com.arangodb.entity;

/**
 * @since ArangoDB 3.10
 */
public class QueryOptimizerRule implements Entity {
    private String name;
    private Flags flags;

    public String getName() {
        return name;
    }

    public Flags getFlags() {
        return flags;
    }

    public static class Flags {
        private Boolean hidden;
        private Boolean clusterOnly;
        private Boolean canBeDisabled;
        private Boolean canCreateAdditionalPlans;
        private Boolean disabledByDefault;
        private Boolean enterpriseOnly;

        public Boolean getHidden() {
            return hidden;
        }

        public Boolean getClusterOnly() {
            return clusterOnly;
        }

        public Boolean getCanBeDisabled() {
            return canBeDisabled;
        }

        public Boolean getCanCreateAdditionalPlans() {
            return canCreateAdditionalPlans;
        }

        public Boolean getDisabledByDefault() {
            return disabledByDefault;
        }

        public Boolean getEnterpriseOnly() {
            return enterpriseOnly;
        }
    }
}
