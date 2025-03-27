package com.arangodb.entity;

import java.util.Objects;

/**
 * @since ArangoDB 3.10
 */
public final class QueryOptimizerRule {
    private String name;
    private Flags flags;

    public String getName() {
        return name;
    }

    public Flags getFlags() {
        return flags;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryOptimizerRule)) return false;
        QueryOptimizerRule that = (QueryOptimizerRule) o;
        return Objects.equals(name, that.name) && Objects.equals(flags, that.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, flags);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Flags)) return false;
            Flags flags = (Flags) o;
            return Objects.equals(hidden, flags.hidden) && Objects.equals(clusterOnly, flags.clusterOnly) && Objects.equals(canBeDisabled, flags.canBeDisabled) && Objects.equals(canCreateAdditionalPlans, flags.canCreateAdditionalPlans) && Objects.equals(disabledByDefault, flags.disabledByDefault) && Objects.equals(enterpriseOnly, flags.enterpriseOnly);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hidden, clusterOnly, canBeDisabled, canCreateAdditionalPlans, disabledByDefault, enterpriseOnly);
        }
    }
}
